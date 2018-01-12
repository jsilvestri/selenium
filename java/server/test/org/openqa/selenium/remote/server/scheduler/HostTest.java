package org.openqa.selenium.remote.server.scheduler;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.openqa.selenium.testing.Assertions.assertException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openqa.selenium.remote.server.SessionFactory;

public class HostTest {

  @Rule
  public ExpectedException expectedException = ExpectedException.none();

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

}
