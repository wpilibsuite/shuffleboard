package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.util.UtilityClassTest;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataSourceUtilsTest extends UtilityClassTest<DataSourceUtils> {

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
    assertEquals(expectedResult, DataSourceUtils.isMetadata(key));
  }

  @ParameterizedTest
  @CsvSource({"foo, foo",
                 "foo, /foo",
                 "foo, ////foo",
                 "bar, /foo/bar",
                 "bar, foo/bar",
                 "abc, a//b//c////abc"})
  public void baseNameTest(String expectedResult, String path) {
    assertEquals(expectedResult, DataSourceUtils.baseName(path));
  }

}