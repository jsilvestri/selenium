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

  public static RetryDelay upTo(Duration maximumTimeout) {
    long remaining = maximumTimeout.toMillis();
    RetryDelay delay = immediately();

    long nextDelay = 500;
    long maxDelay = 5000;
    while (remaining > 0) {
      delay = delay.orElse(retryAfter(Duration.ofMillis(nextDelay)));
      remaining -= nextDelay;
      nextDelay = Math.min(maxDelay, nextDelay + 500);
    }

    return delay;
  }

  public static RetryDelay defaultStrategy() {
    return immediately();
  }
}
