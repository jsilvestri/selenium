package org.openqa.selenium.remote.server.scheduler;

import static java.util.function.Predicate.isEqual;
import static org.openqa.selenium.remote.server.scheduler.Host.Status.UP;

import com.google.common.annotations.VisibleForTesting;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.server.SessionFactory;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Distributor {

  private final CloseableReadWriteLock hostsLock = new CloseableReadWriteLock();
  private final Set<Host> hosts;

  public Distributor() {
    Comparator<Host> hostComparator = Comparator.comparing(Host::getResourceUsage).thenComparing(Host::getUri);
    this.hosts = new ConcurrentSkipListSet<>(hostComparator);
  }

  public Distributor addHost(Host host) {
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
      // Optimistic path
      Optional<Optional<SessionFactory>> found = getHosts(isEqual(UP))
          .map(host -> host.match(capabilities))
          .filter(Optional::isPresent)
          .findFirst();

      if (found.isPresent()) {
        return found.get();
      }

      // No match made. Do any hosts support the capability at all?
      boolean anyPossibleHandlers = getHosts(host -> true)
          .map(host -> host.isSupporting(capabilities))
          .reduce(false, Boolean::logicalOr);

      // Well, alright. Let something have another go at starting the session
      if (anyPossibleHandlers) {
        return Optional.empty();
      }

      throw new SessionNotCreatedException(
          "No known hosts support these capabilities: " + capabilities);
    }
  }

  @VisibleForTesting
  Stream<Host> getHosts(Predicate<Host.Status> condition) {
    try (CloseableLock readLock = hostsLock.lockReadLock()) {
      return hosts.stream().filter(host -> condition.test(host.getStatus()));
    }
  }
}
