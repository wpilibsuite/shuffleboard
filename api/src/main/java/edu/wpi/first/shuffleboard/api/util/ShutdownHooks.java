package edu.wpi.first.shuffleboard.api.util;

import com.google.common.annotations.VisibleForTesting;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Handles shutdown hooks to run when the shuffleboard app closes. Hooks are run in the order in which they are added,
 * and will run on the JavaFX application thread.
 *
 * <p>This class exists mostly because hooks registered via {@link Runtime#addShutdownHook} are not guaranteed to run
 * when shutting down a JavaFX application, and because they run in a nondeterministic order.
 */
public final class ShutdownHooks {

  private static final Queue<Hook> hooks = new ConcurrentLinkedQueue<>();
  private static volatile boolean runningHooks = false;

  private ShutdownHooks() {
    throw new UnsupportedOperationException("This is a utility class");
  }

  @FunctionalInterface
  public interface Hook {
    void run() throws Exception;
  }

  /**
   * Adds a hook to be run at shutdown. If the hooks are already running, this has no effect.
   */
  public static void addHook(Hook hook) {
    if (runningHooks) {
      return;
    }
    hooks.add(hook);
  }

  /**
   * Removes a hook from running at shutdown. If the hooks are already running, this has no effect.
   */
  public static void removeHook(Hook hook) {
    if (runningHooks) {
      return;
    }
    hooks.remove(hook);
  }

  /**
   * Runs all hooks. If a hook throws an exception, it is handled by the current thread's
   * {@link java.lang.Thread.UncaughtExceptionHandler UncaughtExceptionHandler}.
   */
  public static void runAllHooks() {
    runningHooks = true;
    for (Hook hook : hooks) {
      try {
        hook.run();
      } catch (Exception e) {
        Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
      }
    }
  }

  @VisibleForTesting
  static void reset() {
    runningHooks = false;
  }

  @VisibleForTesting
  static void removeAllHooks() {
    if (runningHooks) {
      return;
    }
    hooks.clear();
  }

}
