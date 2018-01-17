package org.openqa.selenium.remote.server.scheduler;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openqa.selenium.testing.Assertions.assertException;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.NewSessionPayload;
import org.openqa.selenium.remote.server.SessionFactory;

import java.io.IOException;
import java.util.Optional;

public class SchedulerTest {

  // This is the original behaviour from Grid. Try, and then fail.
  @Test
  public void byDefaultASchedulerDoesNotAttemptToRescheduleSessions() throws IOException {
    SessionFactory factory = mock(SessionFactory.class);
    when(factory.isSupporting(any())).thenReturn(true);
    Mockito.when(factory.apply(any(), any())).thenReturn(Optional.empty());

    Host host = Host.builder()
        .name("localhost")
        .add(factory)
        .create();

    Distributor distributor = new Distributor().add(host);

    Scheduler scheduler = Scheduler.builder()
        .distributeUsing(distributor)
        .create();

    try (NewSessionPayload payload = NewSessionPayload.create(new FirefoxOptions())) {
      assertException(
          () -> scheduler.createSession(payload),
          e -> assertTrue(e instanceof SessionNotCreatedException));
    }

    verify(factory, times(1)).apply(any(), any());
  }

  @Test
  public void shouldAllowATimedFallback() throws IOException {
    SessionFactory factory = mock(SessionFactory.class);
    when(factory.isSupporting(any())).thenReturn(true);
    // Fail the first call, succeed on the second
    when(factory.apply(any(), any()))
        .thenReturn(
            Optional.empty(),
            Optional.of(new FakeActiveSession(ImmutableSet.of(), new FirefoxOptions())));

    Distributor distributor = new Distributor()
        .add(Host.builder().name("localhost").add(factory).create());

    Scheduler scheduler = Scheduler.builder()
        .distributeUsing(distributor)
        .retrySchedule(RetryDelays.immediately())
        .create();

    try (NewSessionPayload payload = NewSessionPayload.create(new FirefoxOptions())) {
      scheduler.createSession(payload);
    }

    verify(factory, times(2)).apply(any(), any());
  }

  @Test
  public void shouldAllowFlakySessionRestartsWithoutNeedingTimeout() throws IOException {
    SessionFactory factory = mock(SessionFactory.class);
    when(factory.isSupporting(any())).thenReturn(true);
    // Fail the first call, succeed on the second
    when(factory.apply(any(), any()))
        .thenReturn(
            Optional.empty(),
            Optional.of(new FakeActiveSession(ImmutableSet.of(), new FirefoxOptions())));

    Distributor distributor = new Distributor()
        .add(Host.builder().name("localhost").add(factory).create());

    Scheduler scheduler = Scheduler.builder()
        .distributeUsing(distributor)
        .retryFlakyStarts(2)
        .create();

    try (NewSessionPayload payload = NewSessionPayload.create(new FirefoxOptions())) {
      scheduler.createSession(payload);
    }

    verify(factory);
  }
}
