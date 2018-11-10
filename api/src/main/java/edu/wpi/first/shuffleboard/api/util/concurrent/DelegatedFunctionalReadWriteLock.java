package edu.wpi.first.shuffleboard.api.util.concurrent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

final class DelegatedFunctionalReadWriteLock implements FunctionalReadWriteLock {
  private final ReadWriteLock delegate;

  DelegatedFunctionalReadWriteLock(ReadWriteLock delegate) {
    this.delegate = delegate;
  }

  @Override
  public Lock readLock() {
    return delegate.readLock();
  }

  @Override
  public Lock writeLock() {
    return delegate.writeLock();
  }
}
