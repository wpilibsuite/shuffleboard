package edu.wpi.first.shuffleboard.api.util;

import java.util.Arrays;
import java.util.Objects;

public final class EqualityUtils {

  private EqualityUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Checks if two objects are different, ie {@code !isEqual(o1, o2)}.
   *
   * @param o1 the first object to compare
   * @param o2 the second object to compare
   *
   * @return true if the two objects are logically different
   */
  public static boolean isDifferent(Object o1, Object o2) {
    return !isEqual(o1, o2);
  }

  /**
   * Checks if two object arrays are different, ie {@code !isEqual(array1, array2)}.
   *
   * @param array1 the first array to compare
   * @param array2 the second array to compare
   * @param <T>    the component type of the arrays.
   *
   * @return true if the two arrays are logically different
   */
  public static <T> boolean isDifferent(T[] array1, T[] array2) { //NOPMD varargs for last array parameter
    return !isEqual(array1, array2);
  }

  /**
   * Checks if two objects are equal. Unlike {@link Objects#equals(Object, Object) Objects.equals},
   * this works for arrays as well. This only needs to exist because the array equals method is
   * broken.
   *
   * @param o1 the first object to compare
   * @param o2 the second object to compare
   *
   * @return true if the two objects are logically equivalent
   */
  public static boolean isEqual(Object o1, Object o2) {
    if (o1 == null || o2 == null) {
      return o1 == o2; //NOPMD comparing objects using ==; one of these is null so PMD is being dumb
    }
    final Class<?> classA = o1.getClass();
    final Class<?> classB = o2.getClass();

    // check arrays
    if (classA.isArray() && classB.isArray()) {
      final Class<?> typeA = classA.getComponentType();
      final Class<?> typeB = classB.getComponentType();
      if (typeA.equals(typeB)) {
        final Class<?> arrayType = typeA;
        if (arrayType.isPrimitive()) {
          // Primitive arrays
          if (arrayType == int.class) {
            return Arrays.equals((int[]) o1, (int[]) o2);
          } else if (arrayType == double.class) {
            return Arrays.equals((double[]) o1, (double[]) o2);
          } else if (arrayType == byte.class) {
            return Arrays.equals((byte[]) o1, (byte[]) o2);
          } else if (arrayType == short.class) {
            return Arrays.equals((short[]) o1, (short[]) o2);
          } else if (arrayType == char.class) {
            return Arrays.equals((char[]) o1, (char[]) o2);
          } else if (arrayType == boolean.class) {
            return Arrays.equals((boolean[]) o1, (boolean[]) o2);
          } else if (arrayType == float.class) {
            return Arrays.equals((float[]) o1, (float[]) o2);
          }
          throw new UnsupportedOperationException(
              "Cannot compare primitive arrays of type " + arrayType.getSimpleName());
        } else {
          // Object or multi-dimensional arrays
          return isEqual((Object[]) o1, (Object[]) o2);
        }
      } else {
        // Different component types eg int[] and String[]
        return false;
      }
    }

    return Objects.equals(o1, o2);
  }

  /**
   * Checks if two object arrays are logically equal. This does a deep comparison of the two arrays.
   *
   * @param array1 the first array to compare
   * @param array2 the second array to compare
   * @param <T>    the component type of the arrays
   *
   * @return true if the two arrays are logically equal
   */
  public static <T> boolean isEqual(T[] array1, T[] array2) { //NOPMD varargs for last array parameter
    return Arrays.deepEquals(array1, array2);
  }

}
