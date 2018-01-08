package org.openqa.selenium.remote.server.scheduler;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.openqa.selenium.remote.server.scheduler.Host.Status.UP;
import static org.openqa.selenium.remote.server.scheduler.SessionRetryStrategies.immediately;
import static org.openqa.selenium.remote.server.scheduler.SessionRetryStrategies.retryAfter;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.NewSessionPayload;
import org.openqa.selenium.remote.server.ActiveSession;
import org.openqa.selenium.remote.server.ServicedSession;
import org.openqa.selenium.remote.server.SessionFactory;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Logger;

public class Scheduler {

  private final static Logger LOG = Logger.getLogger(Scheduler.class.getName());

  private final ScheduledExecutorService executor;
  private final Distributor distributor;
  private final SessionRetryStrategy retryStrategy;

  private Scheduler(
      Distributor distributor,
      SessionRetryStrategy sessionRetryStrategy) {

    this.distributor = distributor;
    this.retryStrategy = sessionRetryStrategy;

    // What to do with unhandled exceptions? For now we just log them.
    this.executor = new ScheduledThreadPoolExecutor(Runtime.getRuntime().availableProcessors());
  }

  public ActiveSession createSession(Set<Dialect> downstreamDialects, Capabilities capabilities) {

    for (SessionRetryStrategy strategy : retryStrategy) {
      Future<Optional<ActiveSession>> future = schedule(strategy, downstreamDialects, capabilities);

      try {
        Optional<ActiveSession> possibleSession = future.get();
        if (possibleSession.isPresent()) {
          return possibleSession.get();
        }
      } catch (InterruptedException e) {
        Thread.interrupted();
        throw new SessionNotCreatedException("Unable to create session due to interruption", e);
      } catch (ExecutionException e) {
        throw new SessionNotCreatedException("Unable to create session for " + capabilities, e);
      }
    }

    throw new SessionNotCreatedException(
        "Unable to create session for capabilities: " + capabilities);
  }

  private Future<Optional<ActiveSession>> schedule(
      SessionRetryStrategy strategy,
      Set<Dialect> downstreamDialects,
      Capabilities capabilities) {

    Duration delay;
    if (strategy instanceof Delayed) {
      delay = ((Delayed) strategy).getDelay();
    } else {
      delay = Duration.ZERO;
    }

    return executor.schedule(
        () -> {
          try {
            Optional<SessionFactory> matched = distributor.match(capabilities);
            if (!matched.isPresent()) {
              return Optional.empty();
            }

            Optional<ActiveSession> session = matched.get().apply(downstreamDialects, capabilities);
            if (session.isPresent()) {
              return session;
            }
            return strategy.attempt(capabilities);
          } catch (Exception e) {
            return Optional.empty();
          }
        },
        delay.toMillis(),
        MILLISECONDS);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private Distributor distributor;
    private SessionRetryStrategy retryStrategy;

    private Builder() {
      this.retryStrategy = SessionRetryStrategies.immediately();
    }

    public Builder distributeUsing(Distributor distributor) {
      this.distributor = Objects.requireNonNull(distributor, "Distributor cannot be null");
      return this;
    }

    public Builder retryUsing(SessionRetryStrategy strategy) {
      retryStrategy = Objects.requireNonNull(strategy, "Strategy must not be null");
      return this;
    }

    public Scheduler create() {
      if (distributor == null) {
        throw new IllegalStateException("Distributor has not be set and is required");
      }

      return new Scheduler(distributor, retryStrategy);
    }
  }

  public static void main(String[] args) throws Exception {
    // Run some tests on the local host, but just the one
    Host localhost = Host.builder()
        .address("localhost")
        .add(new ServicedSession.Factory(
            caps -> "firefox".equals(caps.getBrowserName()),
            GeckoDriverService.class.getName()))
        .create()
        .setStatus(UP);

    // The distributor will spread the load equally over the machines. Because the grid node has
    // the highest capacity, the first test will go there.
    Distributor distributor = new Distributor().addHost(localhost);

    // Am exponential backoff for retrying sessions. Alternatives could be to keep trying until a
    // time limit is reached or to use a different distributor to get sessions.
    SessionRetryStrategy retryStrategy =
        immediately()
            .orElse(retryAfter(Duration.ofMillis(500)))
            .orElse(retryAfter(Duration.ofMillis(1000)))
            .orElse(retryAfter(Duration.ofMillis(2000)));

    // The scheduler is the main API people will use.
    Scheduler scheduler = Scheduler.builder()
        .distributeUsing(distributor)
        .retryUsing(retryStrategy)
        .create();

    // And they do it like this.
    try (NewSessionPayload payload = NewSessionPayload.create(new FirefoxOptions())) {
      ActiveSession session = scheduler.createSession(
          payload.getDownstreamDialects(),
          new FirefoxOptions());
      session.stop();
    }
  }
}
