package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.remote.server.SessionFactory;

import java.util.Objects;

public class Host {

  private final String name;

  private Host(String name) {
    this.name = Objects.requireNonNull(name, "Name must be set");
  }

  public static Builder builder() {
    return new Builder();
  }

  public String getName() {
    return name;
  }

  public static class Builder {

    private String name;

    private Builder() {

    }

    public Builder name(String name) {
      this.name = Objects.requireNonNull(name, "Name cannot be null");
      return this;
    }


    public Builder add(SessionFactory factory) {
      return this;
    }

    public Host create() {
      return new Host(name);
    }
  }

}
