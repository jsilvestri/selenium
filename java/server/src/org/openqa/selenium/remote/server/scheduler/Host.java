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
  private final int maxConcurrentSessions;

  private volatile Status status;

  private Host(String name, ImmutableList<SessionFactory> factories, int maxConcurrentSessions) {
    this.name = Objects.requireNonNull(name, "Name must be set");
    this.maxConcurrentSessions = maxConcurrentSessions;

    this.factories = factories.stream()
      .map(factory -> {
        if (factory instanceof ScheduledSessionFactory) {
          return (ScheduledSessionFactory) factory;
        }
        return new ScheduledSessionFactory(factory);
      })
    .collect(ImmutableList.toImmutableList());

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
    return factories.stream()
        .map(factory -> factory.isSupporting(caps))
        .reduce(false, Boolean::logicalOr);
  }

  // This should probably be a percentage
  public int getRemainingCapacity() {
    if (factories.isEmpty()) {
      return 0;
    }

    int size = factories.size();
    long free = factories.stream().filter(ScheduledSessionFactory::isAvailable).count();

    return Math.round((free / size) * 100);
  }

  public Optional<SessionFactoryAndCapabilities> match(Capabilities caps) {
    if (getSessionCount() >= maxConcurrentSessions) {
      return Optional.empty();
    }

    return factories.stream()
        .filter(factory -> factory.isSupporting(caps))
        .filter(ScheduledSessionFactory::isAvailable)
        .map(factory -> new SessionFactoryAndCapabilities(factory, caps))
        .findFirst();
  }

  public long getSessionCount() {
    return factories.stream()
        .filter(factory -> !factory.isAvailable())
        .count();
  }

  public long getLastSessionCreated() {
    return factories.stream()
        .map(ScheduledSessionFactory::getLastSessionCreated)
        .reduce(Math::max)
        .orElse(0L);
  }

  public static class Builder {

    private String name;
    private ImmutableList.Builder<SessionFactory> factories;
    private int maxSessions = Integer.MAX_VALUE;

    private Builder() {
      this.factories = ImmutableList.builder();
    }

    public Builder name(String name) {
      this.name = Objects.requireNonNull(name, "Name cannot be null");
      return this;
    }

    public Builder add(SessionFactory factory) {
      factories.add(Objects.requireNonNull(factory, "Session factory cannot be null"));
      return this;
    }

    public Host create() {
      return new Host(name, factories.build(), maxSessions);
    }

    public Builder maxSessions(int maxSessionCount) {
      if (maxSessionCount < 1) {
        throw new IllegalArgumentException(
            "Maximum session count must allow at least one session: " + maxSessionCount);
      }
      this.maxSessions = maxSessionCount;
      return this;
    }
  }

  public enum Status {
    UP,
    DRAINING,
    DOWN
  }
}
