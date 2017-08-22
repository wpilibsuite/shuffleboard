package edu.wpi.first.shuffleboard.api.util;

/**
 * Utility class for making sure certain methods may only be called from tests.
 */
public final class TestUtils {

  private TestUtils() {
    throw new UnsupportedOperationException("TestUtils is a utility class!");
  }

  /**
   * Asserts that this method's call stack originated from a test method invocation.
   *
   * @throws IllegalStateException if this method is not called from within a test
   */
  public static void assertRunningFromTest() throws IllegalStateException {
    boolean found = false;
    for (StackTraceElement element : Thread.currentThread().getStackTrace()) {
      if (element.getClassName().contains("org.junit") || element.getClassName().contains("org.testfx")) {
        found = true;
        break;
      }
    }
    if (!found) {
      throw new IllegalStateException("This method can only be called from a test suite");
    }
  }

}
