package org.openqa.selenium.remote.server.scheduler;

import java.time.Duration;

public class RetryDelays {

  private RetryDelays() {
    // Utility class
  }

  public static RetryDelay immediately() {
    return after(Duration.ZERO);
  }

  public static RetryDelay after(Duration duration) {
    return () -> duration;
  }

  public static RetryDelay upTo(Duration maximumTimeout) {
    long remaining = maximumTimeout.toMillis();
    RetryDelay delay = immediately();

    long nextDelay = 500;
    long maxDelay = 5000;
    while (remaining > 0) {
      delay = delay.orElse(after(Duration.ofMillis(nextDelay)));
      remaining -= nextDelay;
      nextDelay = Math.min(maxDelay, nextDelay + 500);
    }

    return delay;
  }
}
