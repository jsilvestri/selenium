package org.openqa.selenium.remote.server.scheduler;

import org.openqa.selenium.Capabilities;

class SessionFactoryAndCapabilities {

  private final ScheduledSessionFactory factory;
  private final Capabilities capabilities;

  SessionFactoryAndCapabilities(ScheduledSessionFactory factory, Capabilities capabilities) {
    this.factory = factory;
    this.capabilities = capabilities;
  }

}
