package edu.wpi.first.shuffleboard.api.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/**
 * Utilities dealing with threading.
 */
public final class ThreadUtils {

  private ThreadUtils() {
  }

  /**
   * A single-threaded ScheduledExecutorService that uses a daemon thread instead of one the doesn't respect shutdown.
   */
  public static ScheduledExecutorService newDaemonScheduledExecutorService() {
    return Executors.newSingleThreadScheduledExecutor(ThreadUtils::makeDaemonThread);
  }

  /**
   * Creates a daemon thread to run the given runnable.
   */
  public static Thread makeDaemonThread(Runnable runnable) {
    Thread thread = new Thread(runnable);
    thread.setDaemon(true);
    return thread;
  }

  /**
   * Runs a function with a synchronization lock.
   *
   * @param lock     the synchronization lock to use
   * @param function the function to run
   */
  public static void withLock(Lock lock, Runnable function) {
    try {
      lock.lock();
      function.run();
    } finally {
      lock.unlock();
    }
  }

  /**
   * Runs a function with a synchronization lock and returns its result.
   *
   * @param lock     the synchronization lock to use
   * @param function the function to run
   * @param <T>      the type of the value returned by the function
   *
   * @return the output of {@code function}
   */
  public static <T> T withLock(Lock lock, Supplier<? extends T> function) {
    try {
      lock.lock();
      return function.get();
    } finally {
      lock.unlock();
    }
  }
}
