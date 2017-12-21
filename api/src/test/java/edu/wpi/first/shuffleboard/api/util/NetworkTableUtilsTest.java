package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NetworkTableUtilsTest extends UtilityClassTest<NetworkTableUtils> {


  @ParameterizedTest
  @CsvSource({"false, /key",
                 "false, /metadata",
                 "false, /~metadata",
                 "false, /metadata~",
                 "true, /~metadata~",
                 "false, /METADATA",
                 "false, /~METADATA",
                 "false, /METADATA~",
                 "true, /~METADATA~",
                 "true, /.metadata",
                 "true, /~METADATA~/someOtherValue",
                 "true, /.metadata/someOtherValue",
                 "false, /my.key.with.dots",
                 "false, /my~key~with~tildes~",
                 "false, /~my~keywithtildes",
                 "false, /~metadata~with~tildes~"})
  public void isMetaDataTest(boolean expectedResult, String key) {
    assertEquals(expectedResult, NetworkTableUtils.isMetadata(key));
  }

  private static Stream<Arguments> concatArguments() {
    return Stream.of(
        Arguments.of("/foo/bar", "foo", "bar", new String[0]),
        Arguments.of("/one/two/three/four", "one", "two", new String[]{"three", "four"}),
        Arguments.of("/one/two", "/////one////", "///two", new String[0])
    );
  }

  @ParameterizedTest
  @MethodSource(value = "concatArguments")
  public void concatTest(String expectedResult, String value1, String value2, String... more) {
    assertEquals(expectedResult, NetworkTableUtils.concat(value1, value2, more));
  }

}
