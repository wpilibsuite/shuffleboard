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