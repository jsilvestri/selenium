package org.openqa.selenium.remote.server.scheduler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import org.openqa.selenium.remote.server.ActiveSession;

import java.time.Duration;
import java.util.Iterator;
import java.util.Objects;

/**
 * Describes a mechanism for how to reschedule failed attempts to create a new
 * {@link ActiveSession}.
 */
public interface RetryDelay extends Iterable<RetryDelay> {

  default Duration getDelay() {
    return Duration.ZERO;
  }

  default Iterator<RetryDelay> iterator() {
    return ImmutableList.of(this).iterator();
  }

  default RetryDelay orElse(RetryDelay nextStrategy) {
    Objects.requireNonNull(nextStrategy, "Next strategy must not be null");

    return new RetryDelay() {
      @Override
      public Duration getDelay() {
        return nextStrategy.getDelay();
      }

      @Override
      public Iterator<RetryDelay> iterator() {
        return Iterators.concat(ImmutableList.of(this).iterator(), nextStrategy.iterator());
      }
    };
  }
}
