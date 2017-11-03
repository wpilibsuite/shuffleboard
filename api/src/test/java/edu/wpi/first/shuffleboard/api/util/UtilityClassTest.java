package edu.wpi.first.shuffleboard.api.util;

import com.google.common.reflect.TypeToken;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("PMD.AbstractClassWithoutAbstractMethod")
public abstract class UtilityClassTest<T> {

  private final Class<? super T> clazz;

  public UtilityClassTest() {
    this.clazz = (new TypeToken<T>(getClass()) {}).getRawType();
  }

  @TestFactory
  public Stream<DynamicTest> constructorsPrivateDynamicTests() {
    return DynamicTest.stream(Arrays.asList(clazz.getDeclaredConstructors()).iterator(), Constructor::getName,
        constructor -> assertTrue(Modifier.isPrivate(constructor.getModifiers()),
            String.format("The Utility class %s has a non-private constructor.", clazz.getName())));
  }

  @TestFactory
  public Stream<DynamicTest> constructorsReflectionDynamicTests() {
    return DynamicTest.stream(Arrays.asList(clazz.getDeclaredConstructors()).iterator(), Constructor::getName,
        constructor -> assertThrows(InvocationTargetException.class, () -> {
          constructor.setAccessible(true);
          constructor.newInstance();
        }, String.format("The Utility class %s has a non-private constructor.", clazz.getName())));
  }
}
