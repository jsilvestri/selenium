package org.openqa.selenium.remote.server.scheduler;

import com.google.common.collect.ImmutableList;

import org.openqa.selenium.remote.server.SessionFactory;

import java.util.Objects;

public class Host {

  private final String name;
  private final ImmutableList<ScheduledSessionFactory> factories;

  private Host(String name, ImmutableList<ScheduledSessionFactory> factories) {
    this.name = Objects.requireNonNull(name, "Name must be set");
    this.factories = Objects.requireNonNull(factories, "No session factories at all");
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getName() {
    return name;
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

}
