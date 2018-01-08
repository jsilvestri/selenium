package org.openqa.selenium.remote.server.scheduler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;

import java.time.Duration;
import java.util.Iterator;
import java.util.Objects;

abstract class AbstractRetryStrategy implements Delayed, SessionRetryStrategy {

  private final SessionRetryStrategy parent;
  private final Duration delay;

  AbstractRetryStrategy(SessionRetryStrategy parent, Duration delay) {
    this.parent = parent;
    this.delay = Objects.requireNonNull(delay, "Delay must be set");
  }

  @Override
  public Duration getDelay() {
    return delay;
  }

  @Override
  public Iterator<SessionRetryStrategy> iterator() {
    Iterator<SessionRetryStrategy> ownIterator = ImmutableList.<SessionRetryStrategy>of(this).iterator();

    if (parent == null) {
      return ownIterator;
    }

    return Iterators.concat(parent.iterator(), ownIterator);
  }
}
