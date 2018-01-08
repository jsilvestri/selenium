package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.server.ActiveSession;

import java.time.Duration;
import java.util.Optional;

public class SessionRetryStrategies {

  private SessionRetryStrategies() {
    // Utility class.
  }

  public static SessionRetryStrategy immediately() {
    return retryAfter(Duration.ZERO);
  }

  public static SessionRetryStrategy retryAfter(Duration duration) {
    return new AbstractRetryStrategy(null, duration) {
      @Override
      public Optional<ActiveSession> attempt(Capabilities capabilities) {
        System.out.println("duration = " + duration);
        return Optional.empty();
      }
    };
  }

  public static SessionRetryStrategy defaultStrategy() {
    return immediately();
  }
}
