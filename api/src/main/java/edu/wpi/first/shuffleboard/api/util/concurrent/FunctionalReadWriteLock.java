package edu.wpi.first.shuffleboard.api.util.concurrent;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * An extension to {@code ReadWriteLock} that adds methods for using the read and write locks in a functional style.
 */
public interface FunctionalReadWriteLock extends ReadWriteLock {

  /**
   * Creates a new functional read/write lock that uses the read and write locks from the provided delegate.
   *
   * @param delegate the lock to delegate to
   *
   * @return a new functional read/write lock
   */
  static FunctionalReadWriteLock create(ReadWriteLock delegate) {
    return new DelegatedFunctionalReadWriteLock(delegate);
  }

  /**
   * Creates a new functional read/write lock uses reentrant locks.
   *
   * @return a new functional read/write lock
   *
   * @see ReentrantReadWriteLock
   */
  static FunctionalReadWriteLock createReentrant() {
    return new DelegatedFunctionalReadWriteLock(new ReentrantReadWriteLock());
  }

  /**
   * Performs a read operation.
   *
   * @param operation the read operation to run
   */
  default void reading(Runnable operation) {
    try {
      readLock().lock();
      operation.run();
    } finally {
      readLock().unlock();
    }
  }

  /**
   * Performs a read operation.
   *
   * @param operation the read operation to run
   * @param <T>       the type of the provided value
   *
   * @return the result of the operation
   */
  default <T> T reading(Supplier<? extends T> operation) {
    try {
      readLock().lock();
      return operation.get();
    } finally {
      readLock().unlock();
    }
  }

  /**
   * Performs a write operation.
   *
   * @param operation the write operation to run
   */
  default void writing(Runnable operation) {
    try {
      writeLock().lock();
      operation.run();
    } finally {
      writeLock().unlock();
    }
  }

  /**
   * Performs a write operation.
   *
   * @param operation the write operation to run
   * @param <T>       the type of the provided value
   *
   * @return the result of the operation
   */
  default <T> T writing(Supplier<? extends T> operation) {
    try {
      writeLock().lock();
      return operation.get();
    } finally {
      writeLock().unlock();
    }
  }
}
