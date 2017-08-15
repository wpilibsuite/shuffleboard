package edu.wpi.first.shuffleboard.api.util;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple class for debouncing method calls. Debouncing is used to prevent an expensive method from being called
 * in rapid succession, only allowing it to run after a certain amount of time has passed without it being called
 * again.
 */
public class Debouncer {

  private ScheduledFuture<?> future = null;
  private final ScheduledExecutorService executorService;

  private final Duration debounceDelay;

  /**
   * Creates a new debouncer.
   *
   * @param debounceDelay the maximum time delta between calls that should be allowed
   */
  public Debouncer(Duration debounceDelay) {
    this.debounceDelay = debounceDelay;
    executorService = Executors.newSingleThreadScheduledExecutor(ThreadUtils::makeDaemonThread);
  }

  /**
   * Debounces a function call. Generally, this should only be used to debounce multiple calls to the same method/lambda
   * expression.
   *
   * @param runnable the functional unit to be run
   */
  public void debounce(Runnable runnable) {
    if (future != null && !future.isDone()) {
      future.cancel(false);
    }
    future = executorService.schedule(runnable, debounceDelay.toMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Gets the maximum amount of time after a method has been called to wait before running it.
   */
  public Duration getDebounceDelay() {
    return debounceDelay;
  }

}
