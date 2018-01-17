package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.server.ActiveSession;
import org.openqa.selenium.remote.server.SessionFactory;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

class ScheduledSessionFactory implements SessionFactory {

  private final SessionFactory delegate;
  private volatile boolean available = true;
  private volatile long lastUsed = 0;

  ScheduledSessionFactory(SessionFactory delegate) {
    this.delegate = Objects.requireNonNull(delegate, "Actual session factory cannot be null");
  }

  @Override
  public boolean isSupporting(Capabilities capabilities) {
    return delegate.isSupporting(capabilities);
  }

  @Override
  public Optional<ActiveSession> apply(Set<Dialect> downstreamDialects, Capabilities capabilities) {
    lastUsed = System.currentTimeMillis();
    available = false;
    return delegate.apply(downstreamDialects, capabilities);
  }

  public boolean isAvailable() {
    return available;
  }

  public long getLastSessionCreated() {
    return lastUsed;
  }

  public void setAvailable(boolean available) {

  }
}
