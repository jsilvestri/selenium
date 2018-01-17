package org.openqa.selenium.remote.server.scheduler;

import static java.util.logging.Level.WARNING;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.server.ActiveSession;
import org.openqa.selenium.remote.server.SessionFactory;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class SessionFactoryAndCapabilities {

  private final Logger LOG = Logger.getLogger(SessionFactory.class.getName());

  private final ScheduledSessionFactory factory;
  private final Capabilities capabilities;

  SessionFactoryAndCapabilities(ScheduledSessionFactory factory, Capabilities capabilities) {
    this.factory = factory;
    this.capabilities = capabilities;
  }

  public ScheduledSessionFactory getSessionFactory() {
    return factory;
  }

  public Optional<ActiveSession> newSession(Set<Dialect> downstreamDialects) {
    try {
      return factory.apply(downstreamDialects, capabilities);
    } catch (SessionNotCreatedException e) {
      LOG.log(WARNING, "Session not created: " + e.getMessage(), e);
    } catch (Throwable e) {
      LOG.log(WARNING, "Session not created for unexpected reason: " + e.getMessage(), e);
    }
    return Optional.empty();
  }
}
