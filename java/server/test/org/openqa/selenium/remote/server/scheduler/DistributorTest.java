package org.openqa.selenium.remote.server.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.openqa.selenium.SessionNotCreatedException;

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

  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldForbidAddingIdenticalHostsToScheduler() {
    fail("Write me");
  }

  @Test
  public void shouldIncludeHostsThatAreUpInHostList() {
    fail("Write me");
  }

  @Test
  public void itShouldBeFineIfThereAreNoUpMatchingSessionFactories() {
    fail("Write me");  }

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
  public void shouldNotUseHostsThatAreDraining() {
    fail("Write me");
  }

  @Test
  public void shouldPriotiseHostsWithTheMostSlotsAvailableForASessionType() {
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
