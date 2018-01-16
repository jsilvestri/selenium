package org.openqa.selenium.remote.server.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.openqa.selenium.testing.Assertions.assertException;

import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.remote.server.ServicedSession;
import org.openqa.selenium.remote.server.SessionFactory;

public class HostTest {

  @Test
  public void hostsMustHaveAName() {
    SessionFactory factory = mock(SessionFactory.class);

    Host.Builder builder = Host.builder().add(factory);
    assertException(
        builder::create,
        e -> assertTrue(e.getMessage(), e.getMessage().contains("Name")));

    builder.name("localhost");
    Host host = builder.create();  // This should be fine.

    assertEquals("localhost", host.getName());
  }

  @Test
  public void hostsMayHaveNoSessionFactories() {
    Host host = Host.builder().name("localhost").create();

    assertNotNull(host);
  }

  @Test
  public void lastAccessedTimeIsCalculatedFromSessionFactories() {
    ScheduledSessionFactory oldest = mock(ScheduledSessionFactory.class, "oldest");
    when(oldest.getLastSessionCreated()).thenReturn(50L);
    ScheduledSessionFactory youngest = mock(ScheduledSessionFactory.class, "youngest");
    when(youngest.getLastSessionCreated()).thenReturn(100L);

    Host host = Host.builder().name("localhost").add(oldest).add(youngest).create();

    assertEquals(100, host.getLastSessionCreated());
  }

  @Test
  public void shouldAskSessionFactoriesToDetermineWhetherCapabilitiesAreSupported() {
    ServicedSession.Factory firefox = new ServicedSession.Factory(
        caps -> "firefox".equals(caps.getBrowserName()),
        GeckoDriverService.class.getName());

    ServicedSession.Factory chrome = new ServicedSession.Factory(
        caps -> "chrome".equals(caps.getBrowserName()),
        ChromeDriverService.class.getName());

    Host host = Host.builder().name("localhost").add(firefox).add(chrome).create();

    assertTrue(host.isSupporting(new FirefoxOptions()));
    assertTrue(host.isSupporting(new ChromeOptions()));
    assertFalse(host.isSupporting(new EdgeOptions()));
  }

}
