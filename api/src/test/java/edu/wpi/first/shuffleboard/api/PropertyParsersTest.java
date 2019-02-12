package edu.wpi.first.shuffleboard.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Optional;
import java.util.stream.Stream;

import javafx.scene.paint.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PropertyParsersTest {

  private PropertyParsers parsers;

  @BeforeEach
  public void setup() {
    parsers = new PropertyParsers();
  }

  @ParameterizedTest
  @CsvSource({"1.0,1.0", "-2,-2", "3.141592653589,3.141592653589"})
  public void testParseDouble(double input, double expectedOutput) {
    Optional<Double> result = parsers.parse(input, Double.class);
    assertTrue(result.isPresent(), "No result");
    assertEquals(expectedOutput, result.get().doubleValue(), "Unexpected parse result");
  }

  @ParameterizedTest
  @CsvSource({"1,1", "0,0", "65536,65536"})
  public void testParseInteger(int input, int expectedOutput) {
    Optional<Integer> result = parsers.parse(input, Integer.class);
    assertTrue(result.isPresent(), "No result");
    assertEquals(expectedOutput, result.get().intValue(), "Unexpected parse result");
  }

  @ParameterizedTest
  @CsvSource({"1,1", "12.34,12.34", "abc,abc"})
  public void testStringParse(Object input, String expectedOutput) {
    Optional<String> result = parsers.parse(input, String.class);
    assertTrue(result.isPresent(), "No result");
    assertEquals(expectedOutput, result.get(), "Unexpected parse result");
  }

  @ParameterizedTest
  @MethodSource("colorArgs")
  public void testColor(Object input, Color expectedOutput) {
    Optional<Color> result = parsers.parse(input, Color.class);
    assertTrue(result.isPresent(), "No result");
    assertEquals(expectedOutput, result.get(), "Unexpected parse result");
  }

  private static Stream<Arguments> colorArgs() {
    return Stream.of(
        Arguments.of(0xFFFFFFFF, Color.WHITE),  // numeric input
        Arguments.of(Color.BLACK, Color.BLACK), // raw color input
        Arguments.of("#FF0000", Color.RED),     // web color (basic)
        Arguments.of("rgb(255, 255, 255)", Color.WHITE) // web color (advanced)
    );
  }
}