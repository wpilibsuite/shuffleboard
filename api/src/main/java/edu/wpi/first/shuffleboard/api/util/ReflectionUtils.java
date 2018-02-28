package edu.wpi.first.shuffleboard.api.util;

import java.lang.reflect.Field;

/**
 * Utility class for working with reflection.
 */
public final class ReflectionUtils {

  private ReflectionUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Gets the value of a field.
   *
   * @param instance the instance of the class to get the field's value from
   * @param field    the field to get the value of
   * @param <T>      the type of object in the field
   *
   * @return the value of the field
   *
   * @throws ReflectiveOperationException if the value of the field could not be retrieved
   */
  public static <T> T get(Object instance, Field field) throws ReflectiveOperationException {
    field.setAccessible(true);
    return (T) field.get(instance);
  }

  /**
   * Gets the value of a field. Reflective exceptions are wrapped and re-thrown as a runtime exception.
   *
   * @param instance the instance of the class to get the field's value from
   * @param field    the field to get the value of
   * @param <T>      the type of object in the field
   *
   * @return the value of the field
   *
   * @throws RuntimeException if a reflective exception was thrown while attempting to get the value of the field
   */
  public static <T> T getUnchecked(Object instance, Field field) {
    try {
      return get(instance, field);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException("Could not read field: " + field.toGenericString(), e);
    }
  }

}
