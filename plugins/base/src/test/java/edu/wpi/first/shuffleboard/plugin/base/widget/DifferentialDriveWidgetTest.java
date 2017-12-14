package edu.wpi.first.shuffleboard.plugin.base.widget;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DifferentialDriveWidgetTest {

  @ParameterizedTest
  @MethodSource("createMapArgs")
  public void testMap(Number expected, Number x, Number minInput, Number maxInput, Number minOutput, Number maxOutput) {
    assertEquals(
        expected.doubleValue(),
        DifferentialDriveWidget.map(
            x.doubleValue(),
            minInput.doubleValue(), maxInput.doubleValue(),
            minOutput.doubleValue(), maxOutput.doubleValue())
    );
  }

  public static Stream<Arguments> createMapArgs() {
    return Stream.of(
        Arguments.of(0, 0, 0, 1, 0, 1),
        Arguments.of(100, 1, 0, 1, 0, 100),
        Arguments.of(100, 1, 0, 1, 99, 100),
        Arguments.of(-1, 0, 0, 1, -1, 0)
    );
  }

}
