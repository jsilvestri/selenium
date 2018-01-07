package org.openqa.selenium.remote.server.scheduler;

import static org.openqa.selenium.remote.server.scheduler.Host.Status.UP;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.server.SessionFactory;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

public class Scheduler {

  private final CloseableReadWriteLock hostsLock = new CloseableReadWriteLock();
  private final Set<Host> hosts;

  public Scheduler() {
    Comparator<Host> hostComparator = Comparator.comparing(Host::getResourceUsage).thenComparing(Host::getUri);
    this.hosts = new ConcurrentSkipListSet<>(hostComparator);
  }

  public Scheduler addHost(Host host) {
    Objects.requireNonNull(host, "Host must not be null");

    // Order of obtaining the locks is important --- a write lock can obtain a read lock, but a
    // read lock cannot upgrade to a write lock.
    try (
        CloseableLock writeLock = hostsLock.lockWriteLock();
        CloseableLock readLock = hostsLock.lockReadLock()) {
      hosts.stream().map(Host::getUri).forEach(hostUri -> {
        if (hostUri.equals(host.getUri())) {
          throw new IllegalArgumentException("Host already known to scheduler: " + host);
        }
      });

      hosts.add(host);
    }

    return this;
  }

  public Optional<SessionFactory> match(Capabilities capabilities) {
    Objects.requireNonNull(capabilities);

    try (CloseableLock readLock = hostsLock.lockReadLock()) {
      return getHosts()
          .map(host -> host.match(capabilities))
          .filter(Optional::isPresent)
          .findFirst()
          .orElse(Optional.empty());
    }
  }

  @VisibleForTesting
  Stream<Host> getHosts() {
    try (CloseableLock readLock = hostsLock.lockReadLock()) {
      return hosts.stream().filter(host -> UP.equals(host.getStatus()));
    }
  }
}
