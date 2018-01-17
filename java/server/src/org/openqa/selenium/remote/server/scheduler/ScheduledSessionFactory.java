package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.server.ActiveSession;
import org.openqa.selenium.remote.server.SessionFactory;

import java.io.IOException;
import java.util.Map;
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
    return delegate.apply(downstreamDialects, capabilities).map(ScheduledSession::new);
  }

  public boolean isAvailable() {
    return available;
  }

  public long getLastSessionCreated() {
    return lastUsed;
  }

  private class ScheduledSession implements ActiveSession {

    @Override
    public SessionId getId() {
      return delegate.getId();
    }

    @Override
    public Dialect getUpstreamDialect() {
      return delegate.getUpstreamDialect();
    }

    @Override
    public Dialect getDownstreamDialect() {
      return delegate.getDownstreamDialect();
    }

    @Override
    public Map<String, Object> getCapabilities() {
      return delegate.getCapabilities();
    }

    @Override
    public TemporaryFilesystem getFileSystem() {
      return delegate.getFileSystem();
    }

    @Override
    public void stop() {
      try {
        delegate.stop();
      } finally {
        available = true;
        lastUsed = System.currentTimeMillis();
      }
    }

    @Override
    public void execute(HttpRequest req, HttpResponse resp) throws IOException {
      delegate.execute(req, resp);
    }

    @Override
    public WebDriver getWrappedDriver() {
      return delegate.getWrappedDriver();
    }

    private final ActiveSession delegate;

    private ScheduledSession(ActiveSession delegate) {
      this.delegate = delegate;
    }
  }
}
