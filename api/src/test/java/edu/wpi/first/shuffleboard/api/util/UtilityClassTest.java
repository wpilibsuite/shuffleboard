package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class UtilityClassTest<T> {

  private final Class<T> clazz;

  public UtilityClassTest(Class<T> clazz) {
    this.clazz = clazz;
  }

  @Test
  public void testConstructorPrivate() throws NoSuchMethodException {
    Constructor<T> constructor = clazz.getDeclaredConstructor();

    assertFalse(constructor.isAccessible());
  }

  @Test
  public void testConstructorReflection() {
    assertThrows(InvocationTargetException.class, () -> {
      Constructor<T> constructor = clazz.getDeclaredConstructor();
      constructor.setAccessible(true);
      constructor.newInstance();
    });
  }
}
