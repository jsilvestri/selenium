package org.openqa.selenium.remote.server.scheduler;

import com.google.common.collect.ImmutableList;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.server.ActiveSession;

import java.time.Duration;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;

/**
 * Describes a mechanism for how to reschedule failed attempts to create a new
 * {@link ActiveSession}.
 */
public interface SessionRetryStrategy extends Iterable<SessionRetryStrategy> {

  Optional<ActiveSession> attempt(Capabilities capabilities);

  default Iterator<SessionRetryStrategy> iterator() {
    return ImmutableList.of(this).iterator();
  }

  default SessionRetryStrategy orElse(SessionRetryStrategy nextStrategy) {
    Objects.requireNonNull(nextStrategy, "Next strategy must not be null");

    Duration delay;
    if (nextStrategy instanceof Delayed) {
      delay = ((Delayed) nextStrategy).getDelay();
    } else {
      delay = Duration.ZERO;
    }

    return new AbstractRetryStrategy(this, delay) {
      @Override
      public Optional<ActiveSession> attempt(Capabilities capabilities) {
        return nextStrategy.attempt(capabilities);
      }
    };
  }
}
