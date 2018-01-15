package org.openqa.selenium.remote.server.scheduler;

import static org.openqa.selenium.remote.server.scheduler.Host.Status.UP;

import com.google.common.collect.ImmutableList;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.server.SessionFactory;

import java.util.Objects;
import java.util.Optional;

public class Host {

  private final String name;
  private final ImmutableList<ScheduledSessionFactory> factories;

  private volatile Status status;

  private Host(String name, ImmutableList<ScheduledSessionFactory> factories) {
    this.name = Objects.requireNonNull(name, "Name must be set");
    this.factories = Objects.requireNonNull(factories, "No session factories at all");

    this.status = UP;
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getName() {
    return name;
  }

  public Status getStatus() {
    return status;
  }

  public Host setStatus(Status status) {
    this.status = Objects.requireNonNull(status, "Status must be set");
    return this;
  }

  public boolean isSupporting(Capabilities caps) {
    return false;
  }

  public Optional<SessionFactoryAndCapabilities> match(Capabilities caps) {
    return factories.stream()
        .filter(factory -> factory.isSupporting(caps))
        .map(factory -> new SessionFactoryAndCapabilities(factory, caps))
        .findFirst();
  }

  public static class Builder {

    private String name;
    private ImmutableList.Builder<ScheduledSessionFactory> factories;

    private Builder() {
      this.factories = ImmutableList.builder();
    }

    public Builder name(String name) {
      this.name = Objects.requireNonNull(name, "Name cannot be null");
      return this;
    }

    public Builder add(SessionFactory factory) {
      factories.add(new ScheduledSessionFactory(factory));
      return this;
    }

    public Host create() {
      return new Host(name, factories.build());
    }
  }

  public enum Status {
    UP,
    DRAINING,
    DOWN;
  }
}
