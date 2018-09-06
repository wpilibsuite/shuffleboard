package edu.wpi.first.shuffleboard.plugin.powerup;

import edu.wpi.first.shuffleboard.plugin.base.data.fms.Alliance;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.ControlWord;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.FmsInfo;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.MatchType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FieldConfigurationTest {

  @ParameterizedTest(name = "Expect {0} - element={1}, alliance={2}, message=\"{3}\"")
  @MethodSource("createConfigArgs")
  public void testGetConfig(Configuration expected, Element element, Alliance alliance, String gameSpecificMessage) {
    assertEquals(expected, FieldConfiguration.getConfig(element, alliance, gameSpecificMessage));
  }

  private static Stream<Arguments> createConfigArgs() {
    return Stream.of(
        Arguments.of(Configuration.RED_LEFT, Element.NEAR_SWITCH, Alliance.RED, "LRR"),
        Arguments.of(Configuration.RED_LEFT, Element.SCALE, Alliance.RED, "RLR"),
        Arguments.of(Configuration.RED_RIGHT, Element.FAR_SWITCH, Alliance.BLUE, "RRL")
    );
  }

  @Test
  public void testParseFmsInfo() {
    FmsInfo info = new FmsInfo("LRL", "", 0, 0, MatchType.NONE, Alliance.BLUE, 0, ControlWord.fromBits(0));
    FieldConfiguration configuration = FieldConfiguration.parseFmsInfo(info);
    assertEquals(Configuration.RED_RIGHT, configuration.getNearSwitch());
    assertEquals(Configuration.RED_LEFT, configuration.getScale());
    assertEquals(Configuration.RED_RIGHT, configuration.getFarSwitch());
  }

}
