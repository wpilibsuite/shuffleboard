package edu.wpi.first.shuffleboard.api.util;

import com.google.common.annotations.VisibleForTesting;

import java.util.function.Consumer;

/**
 * A quick and dirty solution to running certain things asynchronously.
 */
// TODO remove and replace with dependency injection
public final class AsyncUtils {

  private static Consumer<Runnable> asyncRunner = FxUtils::runOnFxThread;

  private AsyncUtils() {
  }

  @VisibleForTesting
  public static void setAsyncRunner(Consumer<Runnable> asyncRunner) {
    AsyncUtils.asyncRunner = asyncRunner;
  }

  public static void runAsync(Runnable runnable) {
    asyncRunner.accept(runnable);
  }

}
