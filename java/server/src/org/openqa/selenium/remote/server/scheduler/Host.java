package org.openqa.selenium.remote.server.scheduler;

import com.google.common.collect.ImmutableList;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.server.SessionFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Represents a host machine that's capable of running selenium sessions. This class is not
 * thread-safe.
 */
public class Host {

  private final URI address;
  private final Map<SessionFactory, Boolean> factories;
  private volatile Status status = Status.DOWN;

  private Host(
      URI address,
      ImmutableList<SessionFactory> factories) {
    this.address = address;

    this.factories = new LinkedHashMap<>();
    factories.forEach(factory -> this.factories.put(factory, true));
  }

  public static Builder builder() {
    return new Builder();
  }

  public URI getUri() {
    return address;
  }

  public float getResourceUsage() {

    return 0f;
  }

  public long getLastSessionCreatedTime() {
    return 0;
  }

  public Host setStatus(Status status) {
    Objects.requireNonNull(status);
    this.status = status;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Host)) {
      return false;
    }
    Host that = (Host) o;
    return Objects.equals(this.address, that.address);
  }

  @Override
  public int hashCode() {
    return Objects.hash(address);
  }

  public Status getStatus() {
    return status;
  }

  public Optional<SessionFactory> match(Capabilities capabilities) {
    return factories.entrySet().stream()
        .filter(Map.Entry::getValue)
        .filter(entry -> entry.getKey().isSupporting(capabilities))
        .peek(entry -> entry.setValue(false))
        .findFirst()
        .map(Map.Entry::getKey);
  }

  public void release(SessionFactory factory) {

  }

  public boolean isSupporting(Capabilities capabilities) {
    return factories.keySet().stream()
        .map(factory -> factory.isSupporting(capabilities))
        .reduce(false, Boolean::logicalOr);
  }

  public static class Builder {

    private final ImmutableList.Builder<SessionFactory> factories = ImmutableList.builder();
    private URI uri;


    private Builder() {
      // Only from the Host.builder()
    }

    // We use a URI so that it's easier to support schemes that Java's URL doesn't understand (such
    // as unix domain sockets) The URI parser is a little laxer
    public Builder address(URI uri) {
      this.uri = uri;
      return this;
    }

    public Builder address(String uri) {
      try {
        return address(new URI(uri));
      } catch (URISyntaxException e) {
        throw new IllegalArgumentException("Address is not a valid URI:" + e.getMessage(), e);
      }
    }

    public Builder add(SessionFactory sessionFactory) {
      factories.add(sessionFactory);
      return this;
    }


    public Host create() {
      return new Host(uri, factories.build());
    }
  }

  public enum Status {
    UP,
    DOWN,
    DRAINING
  }
}
