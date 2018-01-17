package org.openqa.selenium.remote.server.scheduler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import java.time.Duration;
import java.util.Iterator;

public interface RetryDelay extends Iterable<RetryDelay> {

  Duration getDelay();

  @Override
  default Iterator<RetryDelay> iterator() {
    return ImmutableList.of(this).iterator();
  }

  default RetryDelay orElse(RetryDelay nextDelay) {
    return new RetryDelay() {
      @Override
      public Duration getDelay() {
        return nextDelay.getDelay();
      }

      @Override
      public Iterator<RetryDelay> iterator() {
        return Iterators.concat(ImmutableList.of(this).iterator(), nextDelay.iterator());
      }
    };
  }
}
