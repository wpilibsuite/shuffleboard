package edu.wpi.first.shuffleboard.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Utilities dealing with threading
 */
public final class ThreadUtils {

  private ThreadUtils() {
  }

  public static ScheduledExecutorService newDaemonExecutorService() {
    return Executors.newSingleThreadScheduledExecutor(r -> {
      Thread t = new Thread(r);
      t.setDaemon(true);
      return t;
    });
  }
}
