package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.server.ActiveSession;
import org.openqa.selenium.remote.server.SessionFactory;

import java.util.Optional;
import java.util.Set;

class ScheduledSessionFactory implements SessionFactory {

  private final Host host;
  private final SessionFactory delegate;

  ScheduledSessionFactory(Host host, SessionFactory delegate) {
    this.host = host;
    this.delegate = delegate;
  }

  @Override
  public boolean isSupporting(Capabilities capabilities) {
    return delegate.isSupporting(capabilities);
  }

  @Override
  public Optional<ActiveSession> apply(
      Set<Dialect> downstreamDialects,
      Capabilities capabilities) {
    Optional<ActiveSession> created = delegate.apply(downstreamDialects, capabilities);
    if (!created.isPresent()) {
      host.release(delegate);
      return created;
    }

    return Optional.of(new ScheduledSession(host, delegate, created.get()));
  }
}
