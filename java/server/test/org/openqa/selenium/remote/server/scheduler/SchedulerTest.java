package org.openqa.selenium.remote.server.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.openqa.selenium.remote.server.scheduler.Host.Status.DOWN;
import static org.openqa.selenium.remote.server.scheduler.Host.Status.DRAINING;
import static org.openqa.selenium.remote.server.scheduler.Host.Status.UP;

import com.google.common.collect.ImmutableSet;

import org.junit.Test;
import org.mockito.Mockito;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.server.ActiveSessionFactory;
import org.openqa.selenium.remote.server.SessionFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;

public class SchedulerTest {

  @Test
  public void shouldReturnAllHosts() throws URISyntaxException {
    Host first = Host.builder().address(new URI("first")).create();
    first.setStatus(UP);
    Host second = Host.builder().address(new URI("second")).create();
    second.setStatus(UP);

    Scheduler scheduler = new Scheduler()
        .addHost(first)
        .addHost(second);

    Set<Host> allHosts = scheduler.getHosts();
    assertEquals(2, allHosts.size());
    assertTrue(allHosts.contains(first));
    assertTrue(allHosts.contains(second));
  }

  @Test
  public void shouldListHostsWithLightestLoadedFirst() throws URISyntaxException {
    // Create enough hosts so that we avoid the scheduler returning hosts in:
    // * insertion order
    // * reverse insertion order
    // * sorted with most heavily used first
    Host first = stubHost(80, 0, "first");
    Host second = stubHost(10, 0, "second");
    Host third = stubHost(30, 0, "third");
    Host fourth = stubHost(20, 0, "fourth");

    Scheduler scheduler = new Scheduler()
        .addHost(first)
        .addHost(second)
        .addHost(third)
        .addHost(fourth);

    assertEquals(ImmutableSet.of(second, fourth, third, first), scheduler.getHosts());
  }

  @Test
  public void shouldUseLastSessionCreatedTimeAsTieBreaker() throws URISyntaxException {
    Host first = stubHost(0, 80, "first");
    Host second = stubHost(0, 10, "second");
    Host third = stubHost(0, 30, "third");
    Host fourth = stubHost(0, 20, "fourth");

    Scheduler scheduler = new Scheduler()
        .addHost(first)
        .addHost(second)
        .addHost(third)
        .addHost(fourth);

    assertEquals(ImmutableSet.of(second, fourth, third, first), scheduler.getHosts());
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldForbidAddingIdenticalHostsToScheduler() throws URISyntaxException {
    Host first = stubHost(0, 0, "first");
    Host second = stubHost(10, 0, "first");

    new Scheduler().addHost(first).addHost(second);
  }

  @Test
  public void shouldIncludeHostsThatAreUpInHostList() throws URISyntaxException {
    Host up = stubHost(0, 0, "up");
    up.setStatus(UP);
    Host down = stubHost(0, 0, "down");
    down.setStatus(DOWN);
    Host draining = stubHost(0, 0, "draining");
    draining.setStatus(DRAINING);

    Scheduler scheduler = new Scheduler().addHost(up).addHost(down).addHost(draining);

    assertEquals(ImmutableSet.of(up), scheduler.getHosts());
  }

  @Test
  public void itShouldBeFineIfThereAreNoMatchingSessionFactories() {
    Host host = Host.builder().address("first").create().setStatus(UP);

    Optional<SessionFactory> factory = new Scheduler()
        .addHost(host)
        .match(new FirefoxOptions());
  }

  @Test
  public void canScheduleAJobIfThereIsAFactoryThatMatches() {
    fail("Ouch");
  }

  private Host stubHost(float resourceUsage, long lastSessionCreated, String name) {
    Host host = Host.builder().address(name).create();
    host.setStatus(UP);

    Host spy = Mockito.spy(host);
    when(spy.getResourceUsage()).thenReturn(resourceUsage);
    when(spy.getLastSessionCreatedTime()).thenReturn(lastSessionCreated);
    return spy;
  }
}
