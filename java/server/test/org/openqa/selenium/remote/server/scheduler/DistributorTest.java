package org.openqa.selenium.remote.server.scheduler;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.openqa.selenium.SessionNotCreatedException;

public class DistributorTest {

  @Test
  public void shouldReturnAllHosts() {
    fail("Write me");
  }

  @Test
  public void shouldListHostsWithLightestLoadedFirst() {
    // Create enough hosts so that we avoid the scheduler returning hosts in:
    // * insertion order
    // * reverse insertion order
    // * sorted with most heavily used first
    fail("Write me");
  }

  @Test
  public void shouldUseLastSessionCreatedTimeAsTieBreaker() {
    fail("Write me");
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
}
