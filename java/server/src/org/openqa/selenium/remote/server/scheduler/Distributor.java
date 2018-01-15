package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.Capabilities;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class Distributor {

  private final List<Host> hosts;

  public Distributor() {
    this.hosts = new LinkedList<>();
  }

  public Distributor add(Host host) {
    hosts.add(Objects.requireNonNull(host, "Host cannot be null"));
    return this;
  }

  public Optional<SessionFactoryAndCapabilities> match(Capabilities caps) {
    return hosts.stream()
        .map(host -> host.match(caps))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();

  }
}
