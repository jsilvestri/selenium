package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.NewSessionPayload;

import java.io.IOException;
import java.util.Objects;
import java.util.stream.Stream;

class NewSessionRequest {

  private final NewSessionPayload payload;

  public NewSessionRequest(NewSessionPayload payload) {
    this.payload = Objects.requireNonNull(payload, "New session payload cannot be null");
  }

  public Stream<Capabilities> stream() throws IOException {
    return payload.stream();
  }
}
