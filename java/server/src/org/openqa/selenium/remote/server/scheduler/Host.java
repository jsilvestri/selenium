package org.openqa.selenium.remote.server.scheduler;

import com.google.common.collect.ImmutableList;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.server.SessionFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a host machine that's capable of running selenium sessions. This class is not
 * thread-safe.
 */
public class Host {

  private final URI address;
  private final List<ScheduledSessionFactory> factories;
  private volatile Status status = Status.DOWN;

  private Host(
      URI address,
      ImmutableList<SessionFactory> factories) {
    this.address = address;

    this.factories = factories.stream()
        .map(ScheduledSessionFactory::new)
        .collect(ImmutableList.toImmutableList());
  }

  public static Builder builder() {
    return new Builder();
  }

  public URI getUri() {
    return address;
  }

  public float getResourceUsage() {
    return 0f;
  }

  public long getLastSessionCreatedTime() {
    return 0;
  }

  public Host setStatus(Status status) {
    Objects.requireNonNull(status);
    this.status = status;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Host)) {
      return false;
    }
    Host that = (Host) o;
    return Objects.equals(this.address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address);
  }

  public Status getStatus() {
    return status;
  }

  Optional<ScheduledSessionFactory> match(Capabilities capabilities) {
    return factories.stream()
        .filter(factory -> factory.isSupporting(capabilities))
        .filter(ScheduledSessionFactory::isAvailable)
        .findFirst();
  }

  public boolean isSupporting(Capabilities capabilities) {
    return factories.stream()
        .map(factory -> factory.isSupporting(capabilities))
        .reduce(false, Boolean::logicalOr);
  }

  public static class Builder {

    private final ImmutableList.Builder<SessionFactory> factories = ImmutableList.builder();
    private URI uri;


    private Builder() {
      // Only from the Host.builder()
    }

    // We use a URI so that it's easier to support schemes that Java's URL doesn't understand (such
    // as unix domain sockets) The URI parser is a little laxer
    public Builder address(URI uri) {
      this.uri = uri;
      return this;
    }

    public Builder address(String uri) {
      try {
        return address(new URI(uri));
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException("Address is not a valid URI:" + e.getMessage(), e);
      }
    }

    public Builder add(SessionFactory sessionFactory) {
      factories.add(sessionFactory);
      return this;
    }


    public Host create() {
      return new Host(uri, factories.build());
    }

    public Builder limitSessionCount(int max) {
      return this;
    }
  }

  public enum Status {
    UP,
    DOWN,
    DRAINING
  }
}
