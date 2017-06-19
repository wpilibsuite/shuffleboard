package edu.wpi.first.shuffleboard.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Utilities dealing with threading.
 */
public final class ThreadUtils {

  private ThreadUtils() {
  }

  /**
   * A singleThreadScheduledExecutor that uses a daemon thread instead of one the doesn't respect shutdown.
   */
  public static ScheduledExecutorService newDaemonExecutorService() {
    return Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable);
      thread.setDaemon(true);
      return thread;
    });
  }
}
