package org.openqa.selenium.remote.server.scheduler;

import java.time.Duration;

public class RetryDelays {

  private RetryDelays() {
    // Utility class.
  }

  public static RetryDelay immediately() {
    return retryAfter(Duration.ZERO);
  }

  public static RetryDelay retryAfter(Duration duration) {
    return new RetryDelay() {
      @Override
      public Duration getDelay() {
        return duration;
      }
    };
  }

  public static RetryDelay defaultStrategy() {
    return immediately();
  }
}
