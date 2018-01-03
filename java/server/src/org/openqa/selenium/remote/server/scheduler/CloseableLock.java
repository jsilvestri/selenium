package org.openqa.selenium.remote.server.scheduler;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class CloseableLock implements Lock, Closeable {

  private final Lock lock;

  public CloseableLock(Lock lock) {
    this.lock = Objects.requireNonNull(lock);
  }

  @Override
  public void lock() {
    lock.lock();
  }

  @Override
  public void lockInterruptibly() throws InterruptedException {
    lock.lockInterruptibly();
  }

  @Override
  public boolean tryLock() {
    return lock.tryLock();
  }

  @Override
  public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
    return lock.tryLock(time, unit);
  }

  @Override
  public void unlock() {
    lock.unlock();
  }

  @Override
  public Condition newCondition() {
    return lock.newCondition();
  }

  @Override
  public void close() {
    lock.unlock();
  }
}
