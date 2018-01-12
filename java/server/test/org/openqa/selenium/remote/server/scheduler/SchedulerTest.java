package org.openqa.selenium.remote.server.scheduler;

import static org.junit.Assert.fail;

import org.junit.Test;

public class SchedulerTest {

  // This is the original behvaiour from Grid. Try, and then fail.
  @Test
  public void byDefaultASchedulerDoesNotAttemptToRescheduleSessions() {
    fail("Write me");
  }

  @Test
  public void attemptingToStartASessionWhichFailsMarksTheSessionFactoryAsAvailable() {
    fail("Write me");
  }

  @Test
  public void onceASessionStartsTheAssociatedSessionFactoryBecomesUnavailable() {
    fail("Write me");
  }

  @Test
  public void onceASessionStopsTheAssociatedSessionFactoryBecomesAvailable() {
    fail("Write me");
  }

  @Test
  public void callingQuitOnTheWrappedDriverShouldQuitTheSessionAndMakeTheFactoryAvailable() {
    fail("Write me");
  }

  @Test
  public void shouldAllowATimedFallback() {
    fail("Write me");
  }

}
