package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EqualityUtilsTest extends UtilityClassTest<EqualityUtils> {

  @Test
  public void isEqualInvalidPrimativeTest() {
    assertThrows(UnsupportedOperationException.class, () -> EqualityUtils.isEqual(new long[0], new long[0]));
  }

  @Test
  public void isDifferentArrayTest() {
    assertTrue(EqualityUtils.isDifferent(new Object[]{""}, new Object[]{"A"}));
  }

  @Test
  public void isDifferentArrayFalseTest() {
    assertFalse(EqualityUtils.isDifferent(new Object[]{""}, new Object[]{""}));
  }

  @ParameterizedTest
  @MethodSource(value = "isEqualArguments")
  public void isEqualTest(Object o1, Object o2) {
    assertTrue(EqualityUtils.isEqual(o1, o2));
  }

  @ParameterizedTest
  @MethodSource(value = "isDifferentArguments")
  public void isDifferentTest(Object o1, Object o2) {
    assertTrue(EqualityUtils.isDifferent(o1, o2));
  }

  @ParameterizedTest
  @MethodSource(value = "isEqualArguments")
  public void isDifferentFalseTest(Object o1, Object o2) {
    assertFalse(EqualityUtils.isDifferent(o1, o2));
  }

  private static Stream<Arguments> isEqualArguments() {
    return Stream.of(
        Arguments.of(null, null),
        Arguments.of("Test", "Test"),
        Arguments.of(new int[]{1}, new int[]{1}),
        Arguments.of(new double[]{1.1}, new double[]{1.1}),
        Arguments.of(new byte[]{3}, new byte[]{3}),
        Arguments.of(new short[]{4}, new short[]{4}),
        Arguments.of(new char[]{'a'}, new char[]{'a'}),
        Arguments.of(new boolean[]{true}, new boolean[]{true}),
        Arguments.of(new float[]{2.2f}, new float[]{2.2f}),
        Arguments.of(new Object[]{"Str"}, new Object[]{"Str"})
    );
  }

  private static Stream<Arguments> isDifferentArguments() {
    return Stream.of(
        Arguments.of(null, "null"),
        Arguments.of("null", null),
        Arguments.of("", "A"),
        Arguments.of(new int[0], ""),
        Arguments.of(new int[]{1}, new int[]{2}),
        Arguments.of(new double[]{1.1}, new double[]{1.2}),
        Arguments.of(new byte[]{3}, new byte[]{4}),
        Arguments.of(new short[]{4}, new short[]{5}),
        Arguments.of(new char[]{'a'}, new char[]{'b'}),
        Arguments.of(new boolean[]{true}, new boolean[]{false}),
        Arguments.of(new float[]{2.2f}, new float[]{2.3f}),
        Arguments.of(new Object[]{"Test"}, new Object[]{"Tests"}),
        Arguments.of(new int[0], new double[0])
    );
  }

}
