package org.openqa.selenium.remote.server.scheduler;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openqa.selenium.remote.Dialect.OSS;
import static org.openqa.selenium.remote.server.scheduler.Host.Status.DOWN;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.server.SessionFactory;

import java.util.Optional;

public class SchedulerTest {

  @Test
  public void byDefaultASchedulerDoesNotAttemptToRescheduleSessions() {
    SessionFactory factory = mock(SessionFactory.class);
    when(factory.isSupporting(any())).thenReturn(true);
    when(factory.apply(any(), any())).thenReturn(Optional.empty());

    Host host = Host.builder().address("first").add(factory).create().setStatus(DOWN);
    Distributor distributor = new Distributor().addHost(host);

    Scheduler scheduler = Scheduler.builder()
        .distributeUsing(distributor)
        .create();

    try {
      scheduler.createSession(ImmutableSet.of(OSS), new FirefoxOptions());
      fail("Should not create a new session");
    } catch (SessionNotCreatedException ignored) {
      // This is expected.
    }
  }

//  @Test
//  public void shouldAllowASessionToBeScheduled

}
