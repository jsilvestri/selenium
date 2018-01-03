package org.openqa.selenium.remote.server.scheduler;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CloseableReadWriteLock implements ReadWriteLock {

  private final ReadWriteLock delegate = new ReentrantReadWriteLock();

  @Override
  public Lock readLock() {
    return delegate.readLock();
  }

  public CloseableLock lockReadLock() {
    return obtainCloseableLock(delegate.readLock());
  }

  @Override
  public Lock writeLock() {
    return delegate.writeLock();
  }

  public CloseableLock lockWriteLock() {
    return obtainCloseableLock(delegate.writeLock());
  }

  private CloseableLock obtainCloseableLock(Lock lock) {
    lock.lock();
    return new CloseableLock(lock);
  }
}
