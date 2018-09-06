package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ShutdownHooksTest extends UtilityClassTest<ShutdownHooks> {

  @BeforeEach
  public void clearHooks() {
    ShutdownHooks.reset();
    ShutdownHooks.removeAllHooks();
  }

  @Test
  public void testHookRuns() {
    // given
    boolean[] ran = {false};
    ShutdownHooks.Hook hook = () -> ran[0] = true;

    // when
    ShutdownHooks.addHook(hook);
    ShutdownHooks.runAllHooks();

    // then
    assertTrue(ran[0], "Hook did not run");
  }

  @Test
  public void testHookOrder() {
    // given
    long[] timestamps = {-1, -1};
    ShutdownHooks.Hook hook1 = () -> timestamps[0] = System.nanoTime();
    ShutdownHooks.Hook hook2 = () -> timestamps[1] = System.nanoTime();

    // when
    ShutdownHooks.addHook(hook1);
    ShutdownHooks.addHook(hook2);
    ShutdownHooks.runAllHooks();

    // then
    assertTrue(timestamps[0] < timestamps[1], "The first given hook should have run first");
  }

  @Test
  public void testHookException() {
    final Thread.UncaughtExceptionHandler oldHandler = Thread.currentThread().getUncaughtExceptionHandler();

    try {
      final List<Throwable> errors = new ArrayList<>();
      Thread.UncaughtExceptionHandler handler = (__, error) -> errors.add(error);
      Thread.currentThread().setUncaughtExceptionHandler(handler);

      // given
      final Exception toThrow = new Exception("Expected");
      ShutdownHooks.Hook throwing = () -> {
        throw toThrow;
      };
      boolean[] ran = {false};
      ShutdownHooks.Hook hook = () -> ran[0] = true;

      // when
      ShutdownHooks.addHook(throwing);
      ShutdownHooks.addHook(hook);
      ShutdownHooks.runAllHooks();

      // then
      assertAll(
          () -> assertEquals(1, errors.size(), "Only one error should have been thrown, but was: " + errors),
          () -> assertEquals(toThrow, errors.get(0), "An unexpected error was thrown"));
      assertTrue(ran[0], "Normal hook did not run");
    } finally {
      // Reset the uncaught exception handler
      Thread.currentThread().setUncaughtExceptionHandler(oldHandler);
    }
  }

  @Test
  public void testAddHookWhenRunning() {
    // given
    boolean[] ran = {false};
    ShutdownHooks.Hook hook = () -> ran[0] = true;

    // when
    ShutdownHooks.runAllHooks();
    ShutdownHooks.addHook(hook);
    ShutdownHooks.runAllHooks();

    // then
    assertFalse(ran[0], "The hook should not have run");
  }

  @Test
  public void testRemoveHook() {
    // given
    boolean[] ran = {false};
    ShutdownHooks.Hook hook = () -> ran[0] = true;

    // when
    ShutdownHooks.addHook(hook);
    ShutdownHooks.removeHook(hook);
    ShutdownHooks.runAllHooks();

    // then
    assertFalse(ran[0], "The hook should not have run");
  }

}
