package org.openqa.selenium.remote.server.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.server.ServicedSession;
import org.openqa.selenium.remote.server.SessionFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    Set<Host> allHosts = scheduler.getHosts().collect(Collectors.toSet());
    assertEquals(2, allHosts.size());
    assertTrue(allHosts.contains(first));
    assertTrue(allHosts.contains(second));
  }

  @Test
  public void shouldListHostsWithLightestLoadedFirst() {
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

    assertEquals(
        ImmutableSet.of(second, fourth, third, first),
        scheduler.getHosts().collect(ImmutableSet.toImmutableSet()));
  }

  @Test
  public void shouldUseLastSessionCreatedTimeAsTieBreaker() {
    Host first = stubHost(0, 80, "first");
    Host second = stubHost(0, 10, "second");
    Host third = stubHost(0, 30, "third");
    Host fourth = stubHost(0, 20, "fourth");

    Scheduler scheduler = new Scheduler()
        .addHost(first)
        .addHost(second)
        .addHost(third)
        .addHost(fourth);

    assertEquals(
        ImmutableSet.of(second, fourth, third, first),
        scheduler.getHosts().collect(ImmutableSet.toImmutableSet()));
  }

  @Test(expected = IllegalArgumentException.class)
  public void shouldForbidAddingIdenticalHostsToScheduler() {
    Host first = stubHost(0, 0, "first");
    Host second = stubHost(10, 0, "first");

    new Scheduler().addHost(first).addHost(second);
  }

  @Test
  public void shouldIncludeHostsThatAreUpInHostList() {
    Host up = stubHost(0, 0, "up");
    up.setStatus(UP);
    Host down = stubHost(0, 0, "down");
    down.setStatus(DOWN);
    Host draining = stubHost(0, 0, "draining");
    draining.setStatus(DRAINING);

    Scheduler scheduler = new Scheduler().addHost(up).addHost(down).addHost(draining);

    assertEquals(ImmutableSet.of(up), scheduler.getHosts().collect(ImmutableSet.toImmutableSet()));
  }

  @Test
  public void itShouldBeFineIfThereAreNoMatchingSessionFactories() {
    SessionFactory sessionFactory =
        new ServicedSession.Factory(caps -> false, GeckoDriverService.class.getName());

    Host host = Host.builder()
        .address("first")
        .add(sessionFactory)
        .create()
        .setStatus(UP);

    Optional<SessionFactory> seen = new Scheduler()
        .addHost(host)
        .match(new FirefoxOptions());

    assertFalse(seen.isPresent());
  }

  @Test
  public void canScheduleAJobIfThereIsAFactoryThatMatches() {
    SessionFactory sessionFactory =
        new ServicedSession.Factory(caps -> true, GeckoDriverService.class.getName());

    Host host = Host.builder()
        .address("first")
        .add(sessionFactory)
        .create()
        .setStatus(UP);

    Optional<SessionFactory> seen = new Scheduler()
        .addHost(host)
        .match(new FirefoxOptions());

    assertEquals(sessionFactory, seen.get());
  }

  @Test
  public void shouldNotScheduleAJobOnASessionFactoryThatIsAlreadyBeingUsed() {
    SessionFactory sessionFactory =
        new ServicedSession.Factory(caps -> true, GeckoDriverService.class.getName());

    Host host = Host.builder()
        .address("first")
        .add(sessionFactory)
        .create()
        .setStatus(UP);

    Scheduler scheduler = new Scheduler().addHost(host);

    Optional<SessionFactory> seen = scheduler.match(new FirefoxOptions());
    assertEquals(sessionFactory, seen.get());

    seen = scheduler.match(new FirefoxOptions());
    assertFalse(seen.isPresent());
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
