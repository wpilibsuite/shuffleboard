package edu.wpi.first.shuffleboard.api.util;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * A simple class for debouncing method calls. Debouncing is used to prevent an expensive method from being called
 * in rapid succession, only allowing it to run after a certain amount of time has passed without it being called
 * again.
 */
public class Debouncer implements Runnable {

  private ScheduledFuture<?> future = null;
  private final ScheduledExecutorService executorService;

  private final Runnable target;
  private final Duration debounceDelay;

  /**
   * Creates a new debouncer.
   *
   * @param target        the target function that should be debounced
   * @param debounceDelay the maximum time delta between calls that should be allowed
   */
  public Debouncer(Runnable target, Duration debounceDelay) {
    this.target = Objects.requireNonNull(target, "target");
    this.debounceDelay = Objects.requireNonNull(debounceDelay, "debounceDelay");
    executorService = ThreadUtils.newDaemonScheduledExecutorService();
  }

  @Override
  public void run() {
    if (future != null && !future.isDone()) {
      future.cancel(false);
    }
    future = executorService.schedule(target, debounceDelay.toMillis(), TimeUnit.MILLISECONDS);
  }

  /**
   * Gets the maximum amount of time after a method has been called to wait before running it.
   */
  public Duration getDebounceDelay() {
    return debounceDelay;
  }

  /**
   * Cancels the debouncer. The target will not run unless {@link #run()} is called later.
   */
  public void cancel() {
    if (future != null) {
      future.cancel(true);
    }
  }

}
