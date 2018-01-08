package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.server.ActiveSession;

public class Scheduler {

  private Scheduler() {
    // Only accessed through builder.
  }

  public static Builder builder() {
    return new Builder();
  }

  public ActiveSession createSession(Capabilities capabilities) {
    throw new SessionNotCreatedException("Unable to create a new session");
  }

  public static class Builder {
    private Builder() {
      // Helper class
    }

    public Builder distributeUsing(Distributor distributor) {
      return this;
    }

    public Scheduler create() {
      return new Scheduler();
    }
  }
}
