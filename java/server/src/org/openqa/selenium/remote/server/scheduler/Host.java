package org.openqa.selenium.remote.server.scheduler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Host {

  private final URI address;
  private AtomicReference<Status> status = new AtomicReference<>(Status.DOWN);

  private Host(URI address) {
    this.address = address;
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
    this.status.set(status);
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
    return status.get();
  }

  public static class Builder {

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

    public Host create() {
      return new Host(uri);
    }
  }

  public enum Status {
    UP,
    DOWN,
    DRAINING
  }
}
