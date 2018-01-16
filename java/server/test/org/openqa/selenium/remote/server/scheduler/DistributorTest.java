package org.openqa.selenium.remote.server.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.openqa.selenium.remote.server.scheduler.Host.Status.DOWN;
import static org.openqa.selenium.remote.server.scheduler.Host.Status.DRAINING;
import static org.openqa.selenium.remote.server.scheduler.Host.Status.UP;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.server.ServicedSession;
import org.openqa.selenium.testing.Assertions;

import java.util.List;
import java.util.stream.Collectors;

public class DistributorTest {

  @Test
  public void shouldListHostsWithLightestLoadedFirst() {
    // Create enough hosts so that we avoid the scheduler returning hosts in:
    // * insertion order
    // * reverse insertion order
    // * sorted with most heavily used first
    Host lightest = spy(Host.builder().name("light").create());
    when(lightest.getRemainingCapacity()).thenReturn(10);
    when(lightest.isSupporting(any())).thenReturn(true);

    Host medium = spy(Host.builder().name("medium").create());
    when(medium.getRemainingCapacity()).thenReturn(30);
    when(medium.isSupporting(any())).thenReturn(true);

    Host heavy = spy(Host.builder().name("heavy").create());
    when(heavy.getRemainingCapacity()).thenReturn(50);
    when(heavy.isSupporting(any())).thenReturn(true);

    Host massive = spy(Host.builder().name("massive").create());
    when(massive.getRemainingCapacity()).thenReturn(80);
    when(massive.isSupporting(any())).thenReturn(true);

    Distributor distributor = new Distributor()
        .add(heavy)
        .add(medium)
        .add(lightest)
        .add(massive);

    ImmutableList<Host> results = distributor.getHosts().collect(ImmutableList.toImmutableList());

    assertEquals(ImmutableList.of(lightest, medium, heavy, massive), results);
  }

  @Test
  public void shouldUseLastSessionCreatedTimeAsTieBreaker() {
    Host leastRecent = spy(Host.builder().name("first").create());
    when(leastRecent.getLastSessionCreated()).thenReturn(50L);

    Host middle = spy(Host.builder().name("middle").create());
    when(middle.getLastSessionCreated()).thenReturn(100L);

    Host mostRecent = spy(Host.builder().name("latest").create());
    when(mostRecent.getLastSessionCreated()).thenReturn(150L);

    Distributor distributor = new Distributor().add(middle).add(mostRecent).add(leastRecent);

    List<Host> hosts = distributor.getHosts().collect(Collectors.toList());

    assertEquals(ImmutableList.of(mostRecent, middle, leastRecent), hosts);
  }

  @Test
  public void shouldForbidAddingIdenticallyNamedHostsToScheduler() {
    Host first = Host.builder().name("hello").create();
    Host second = Host.builder().name("hello").create();

    Distributor distributor = new Distributor().add(first);

    Assertions.assertException(
        () -> distributor.add(second),
        e -> assertTrue(e instanceof IllegalArgumentException));
  }

  @Test
  public void shouldIncludeHostsThatAreUpInHostList() {
    // It's okay to use the same factory repeatedly
    ServicedSession.Factory factory = new ServicedSession.Factory(
        caps -> "firefox".equals(caps.getBrowserName()),
        GeckoDriverService.class.getName());

    Host up = Host.builder().name("up").add(factory).create().setStatus(UP);
    Host down = Host.builder().name("down").add(factory).create().setStatus(DOWN);
    Host draining = Host.builder().name("draining").add(factory).create().setStatus(DRAINING);

    Distributor distributor = new Distributor().add(up).add(down).add(draining);

    ImmutableList<SessionFactoryAndCapabilities> matches =
        distributor.match(new FirefoxOptions()).collect(ImmutableList.toImmutableList());

    assertEquals(1, matches.size());
  }

  @Test
  public void canScheduleAJobIfThereIsAFactoryThatMatches() {
    fail("Write me");  }

  @Test
  public void shouldNotScheduleAJobOnASessionFactoryThatIsAlreadyBeingUsed() {
    fail("Write me");  }


  @Test(expected = SessionNotCreatedException.class)
  public void shouldThrowAnExceptionIfThereAreNoHostsThatSupportTheGivenSessionType() {
    fail("Write me");
  }

  @Test
  public void shouldNotBeAbleToScheduleMoreSessionsThanAvailableCapacity() {
    fail("Write me");
  }

  @Test
  public void ifAHostGoesDownItShouldBeSkipped() {
    fail("Write me");
  }

  @Test
  public void shouldAHostGoDownAllAssociatedSessionsAreKilled() {
    fail("Write me");
  }

  @Test
  public void selfHealingRemoteHostsAreRegisteredOnceTheyAreOkay() {
    fail("Write me");
  }

  @Test
  public void shouldPriotizeHostsWithTheMostSlotsAvailableForASessionType() {
    // Consider the case where you have 1 Windows machine and 5 linux machines. All of these hosts
    // can run Chrome and Firefox sessions, but only one can run Edge sessions. Ideally, the machine
    // able to run Edge would be sorted last.

    fail("Write me");
  }

  @Test
  public void shouldReturnAStreamWithAllMatchingSessionFactories() {
    fail("Write me");
  }
}
