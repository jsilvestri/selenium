package org.openqa.selenium.remote.server.scheduler;

import static org.openqa.selenium.remote.Dialect.OSS;

import com.google.common.collect.Iterators;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.io.TemporaryFilesystem;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;
import org.openqa.selenium.remote.server.ActiveSession;
import org.openqa.selenium.remote.server.ActiveSessionCommandExecutor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

class FakeActiveSession implements ActiveSession {

  private static final AtomicInteger counter = new AtomicInteger(1);

  private final SessionId id = new SessionId("session" + counter.getAndIncrement());
  private final Dialect downstream;
  private final Capabilities caps;

  FakeActiveSession(Set<Dialect> downstreams, Capabilities caps) {
    this.downstream = Iterators.getNext(downstreams.iterator(), OSS);
    this.caps = caps;
  }

  @Override
  public SessionId getId() {
    return id;
  }

  @Override
  public Dialect getUpstreamDialect() {
    return OSS;
  }

  @Override
  public Dialect getDownstreamDialect() {
    return downstream;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Map<String, Object> getCapabilities() {
    return (Map<String, Object>) caps.asMap();
  }

  @Override
  public TemporaryFilesystem getFileSystem() {
    throw new UnsupportedOperationException("getFileSystem");
  }

  @Override
  public void stop() {
    // no-op
  }

  @Override
  public WebDriver getWrappedDriver() {
    return new RemoteWebDriver(new ActiveSessionCommandExecutor(this), caps);
  }

  @Override
  public void execute(HttpRequest req, HttpResponse resp) {
    resp.setStatus(0);
  }
}
