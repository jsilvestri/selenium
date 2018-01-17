package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.server.ActiveSession;

import java.util.Set;

class SessionFactoryAndCapabilities {

  private final ScheduledSessionFactory factory;
  private final Capabilities capabilities;

  SessionFactoryAndCapabilities(ScheduledSessionFactory factory, Capabilities capabilities) {
    this.factory = factory;
    this.capabilities = capabilities;
  }

  public ActiveSession newSession(Set<Dialect> downstreamDialects) {
    return factory.apply(downstreamDialects, capabilities)
        .map(session -> new ScheduledSession(factory, session))
        .orElseThrow(() -> new SessionNotCreatedException("Unable to create session for " + capabilities));
  }
}
