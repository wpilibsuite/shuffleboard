package edu.wpi.first.shuffleboard.api.util;

import java.util.Arrays;
import java.util.Locale;

public final class StringUtils {

  private StringUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Checks if the base string contains the test string, ignoring uppercase/lowercase differences (using the American
   * English definitions of "upper" and "lower" case).
   *
   * @param base the base text to search in
   * @param test the string to search for
   *
   * @return true if {@code base} contains {@code test}, ignoring case differences
   */
  public static boolean containsIgnoreCase(String base, String test) {
    return base.toLowerCase(Locale.US).contains(test.toLowerCase(Locale.US));
  }

  /**
   * Generates a string representation of any object. This supports {@code null}, primitives, arrays, primitive arrays,
   * and multi-dimensioned arrays of any type.
   */
  public static String deepToString(Object object) {
    if (object == null) {
      return "null";
    }
    if (object instanceof Object[]) {
      return Arrays.deepToString((Object[]) object);
    } else if (object instanceof byte[]) {
      return Arrays.toString((byte[]) object);
    } else if (object instanceof char[]) {
      return Arrays.toString((char[]) object);
    } else if (object instanceof short[]) {
      return Arrays.toString((short[]) object);
    } else if (object instanceof int[]) {
      return Arrays.toString((int[]) object);
    } else if (object instanceof long[]) {
      return Arrays.toString((long[]) object);
    } else if (object instanceof float[]) {
      return Arrays.toString((float[]) object);
    } else if (object instanceof double[]) {
      return Arrays.toString((double[]) object);
    } else if (object instanceof boolean[]) {
      return Arrays.toString((boolean[]) object);
    } else {
      return object.toString();
    }
  }

}
