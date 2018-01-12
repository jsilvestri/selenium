package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.server.ActiveSession;
import org.openqa.selenium.remote.server.SessionFactory;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

class ScheduledSessionFactory implements SessionFactory {

  private final static Logger LOG = Logger.getLogger(ScheduledSessionFactory.class.getName());

  private final SessionFactory delegate;
  private volatile boolean available;

  ScheduledSessionFactory(SessionFactory delegate) {
    this.delegate = delegate;
    this.available = true;
  }

  @Override
  public boolean isSupporting(Capabilities capabilities) {
    return delegate.isSupporting(capabilities);
  }

  public boolean isAvailable() {
    return available;
  }

  public void setAvailable(boolean available) {
    this.available = available;
  }

  @Override
  public Optional<ActiveSession> apply(
      Set<Dialect> downstreamDialects,
      Capabilities capabilities) {
    setAvailable(false);
    try {
      Optional<ActiveSession> created = delegate.apply(downstreamDialects, capabilities);
      if (!created.isPresent()) {
        setAvailable(true);
        return created;
      }

      return Optional.of(new ScheduledSession(this, created.get()));
    } catch (Throwable t) {
      // Mark ourselves available if something goes wrong
      LOG.log(Level.WARNING, t.getMessage(), t);
      setAvailable(true);
      throw t;
    }
  }
}
