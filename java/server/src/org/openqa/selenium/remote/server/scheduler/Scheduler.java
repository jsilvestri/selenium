package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.NewSessionPayload;
import org.openqa.selenium.remote.server.ActiveSession;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class Scheduler {

  private final Distributor distributor;

  private final BlockingDeque<NewSessionRequest> requests;

  private Scheduler(Distributor distributor) {
    this.distributor = Objects.requireNonNull(distributor, "Distributor cannot be null");

    this.requests = new LinkedBlockingDeque<>();
  }

  public static Builder builder() {
    return new Builder();
  }

  public ActiveSession createSession(NewSessionPayload payload) throws SessionNotCreatedException {
    try {
      NewSessionRequest request = new NewSessionRequest(payload);

      request.stream()
          .map(distributor::match)
          .filter(Optional::isPresent)
          .map(Optional::get)
          .findFirst();

      return null;
    } catch (SessionNotCreatedException e) {
      throw e;
    } catch (Exception e) {
      throw new SessionNotCreatedException(e.getMessage(), e);
    }
  }



  public static class Builder {

    private Distributor distributor;

    private Builder() {
      // no-op
    }

    public Builder distributeUsing(Distributor distributor) {
      this.distributor = Objects.requireNonNull(distributor, "Distributor cannot be null");
      return this;
    }

    public Scheduler create() {
      return new Scheduler(distributor);
    }
  }
}
