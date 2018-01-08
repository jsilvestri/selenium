package org.openqa.selenium.remote.server.scheduler;

import java.time.Duration;

public interface Delayed {
  Duration getDelay();
}
