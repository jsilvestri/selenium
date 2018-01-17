package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.server.ActiveSession;

import java.io.IOException;
import java.util.Map;

class ScheduledSession implements ActiveSession {

  private final ScheduledSessionFactory factory;
  private final ActiveSession delegate;

  ScheduledSession(ScheduledSessionFactory factory, ActiveSession delegate) {
    this.factory = factory;
    this.delegate = delegate;
  }

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
    factory.setAvailable(true);
    delegate.stop();
  }

  @Override
  public void execute(HttpRequest req, HttpResponse resp) throws IOException {
    delegate.execute(req, resp);
  }

  @Override
  public WebDriver getWrappedDriver() {
    return delegate.getWrappedDriver();
  }
}
