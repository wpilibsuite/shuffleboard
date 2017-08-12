package edu.wpi.first.shuffleboard.api.util;

import com.google.common.collect.Lists;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NetworkTableUtilsTest extends UtilityClassTest<NetworkTableUtils> {

  public NetworkTableUtilsTest() {
    super(NetworkTableUtils.class);
  }

  @ParameterizedTest
  @CsvSource({"simple, simple",
                 "simple, one/two/many/simple",
                 "simple, //////an/////awful/key////simple"})
  public void simpleKeyTest(String expectedResult, String key) {
    assertEquals(expectedResult, NetworkTableUtils.simpleKey(key));
  }

  @ParameterizedTest
  @CsvSource({"/, //////////////////////////",
                 "/this/doesn't/need/to/be/normalized, /this/doesn't/need/to/be/normalized",
                 "/no/leading/slash, no/leading/slash",
                 "/what/an/awful/key/, //////what////an/awful/////key///"})
  public void normalizeKeyTest(String expectedResult, String key) {
    assertEquals(expectedResult, NetworkTableUtils.normalizeKey(key));
  }

  @ParameterizedTest
  @CsvSource({"a, a",
                 "a, //////////////////////////a",
                 "this/doesn't/need/to/be/normalized, /this/doesn't/need/to/be/normalized",
                 "no/leading/slash, no/leading/slash",
                 "what/an/awful/key/, //////what////an/awful/////key///"})
  public void normalizeKeyNoLeadingTest(String expectedResult, String key) {
    assertEquals(expectedResult, NetworkTableUtils.normalizeKey(key, false));
  }

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

  private static Stream<Arguments> getHierarchyArguments() {
    return Stream.of(
        Arguments.of(Lists.newArrayList("/"), ""),
        Arguments.of(Lists.newArrayList("/"), "/"),
        Arguments.of(Lists.newArrayList("/", "/foo", "/foo/bar", "/foo/bar/baz"), "/foo/bar/baz")
    );
  }

  @ParameterizedTest
  @MethodSource(value = "getHierarchyArguments")
  public void getHierarchyTest(List<String> expectedResult, String key) {
    assertEquals(expectedResult, NetworkTableUtils.getHierarchy(key));
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
