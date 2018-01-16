package org.openqa.selenium.remote.server.scheduler;

import com.google.common.annotations.VisibleForTesting;

import org.openqa.selenium.Capabilities;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class Distributor {

  private final List<Host> hosts;
  // Prefer the lightest loaded, most idle hosts by default.
  private final Comparator<Host> weightingAlgorithm =
      Comparator.comparing(Host::getRemainingCapacity)
          .thenComparing((l, r) -> ((Long) (r.getLastSessionCreated() - l.getLastSessionCreated())).intValue())
          .thenComparing(Host::getName);

  public Distributor() {
    this.hosts = new CopyOnWriteArrayList<>();
  }

  public Distributor add(Host host) {
    Objects.requireNonNull(host, "Host cannot be null");
    boolean exists = hosts.stream()
        .map(h -> h.getName().equals(host.getName()))
        .reduce(false, Boolean::logicalOr);

    if (exists) {
      throw new IllegalArgumentException(
          "A host with the same name has already been added: " + host.getName());
    }

    hosts.add(host);

    return this;
  }

  public Stream<SessionFactoryAndCapabilities> match(Capabilities caps) {
    return getHosts()
        .filter(host -> host.getStatus() == Host.Status.UP)
        .map(host -> host.match(caps))
        .filter(Optional::isPresent)
        .map(Optional::get);
  }

  @VisibleForTesting
  Stream<Host> getHosts() {
    return hosts.stream()
        .sorted(weightingAlgorithm);
  }
}
