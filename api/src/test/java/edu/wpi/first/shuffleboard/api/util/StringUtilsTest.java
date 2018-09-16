package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StringUtilsTest {

  @ParameterizedTest()
  @MethodSource("deepToStringArgs")
  public void testDeepToString(Object object, String expected) {
    assertEquals(expected, StringUtils.deepToString(object));
  }

  public static Stream<Arguments> deepToStringArgs() {
    return Stream.of(
        Arguments.of(null, "null"),
        Arguments.of("a string", "a string"),
        Arguments.of(1, "1"),
        Arguments.of(1.234, "1.234"),
        Arguments.of(new int[]{1, 2, 3}, "[1, 2, 3]"),
        Arguments.of(new double[]{0, 10}, "[0.0, 10.0]"),
        Arguments.of(new long[][]{{1, 2, 3}, {4, 5, 6}}, "[[1, 2, 3], [4, 5, 6]]"),
        Arguments.of(new boolean[][][]{{{true}, {false}}, {{true}}}, "[[[true], [false]], [[true]]]")
    );
  }

  @ParameterizedTest
  @MethodSource("containsArgs")
  public void testContainsIgnoreCase(String base, String test, boolean expectedResult) {
    assertEquals(expectedResult, StringUtils.containsIgnoreCase(base, test));
  }

  public static Stream<Arguments> containsArgs() {
    return Stream.of(
        Arguments.of("", "", true),
        Arguments.of("lower", "low", true),
        Arguments.of("UPPER", "ppe", true),
        Arguments.of("lower", "OW", true)
    );
  }

}