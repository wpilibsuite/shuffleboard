package edu.wpi.first.shuffleboard.plugin.base.data.fms;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.IntPredicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static edu.wpi.first.shuffleboard.api.util.BitUtils.flagMatches;
import static edu.wpi.first.shuffleboard.plugin.base.data.fms.RobotControlState.Autonomous;
import static edu.wpi.first.shuffleboard.plugin.base.data.fms.RobotControlState.Disabled;
import static edu.wpi.first.shuffleboard.plugin.base.data.fms.RobotControlState.Teleoperated;
import static edu.wpi.first.shuffleboard.plugin.base.data.fms.RobotControlState.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class ControlWordTest {

  @ParameterizedTest(name = "[{index}] bits = {0}")
  @MethodSource("createBitArgs")
  public void testFromBits(Integer bits) {
    ControlWord controlWord = ControlWord.fromBits(bits);
    boolean enabled = flagMatches(bits, ControlWord.ENABLED_FLAG);
    boolean auto = flagMatches(bits, ControlWord.AUTO_FLAG);
    boolean test = flagMatches(bits, ControlWord.TEST_FLAG);
    RobotControlState controlState = controlWord.getControlState();
    assertEquals(enabled, controlState != Disabled, "Unexpected control state " + controlState);
    if (enabled) {
      assertNotEquals(Disabled, controlState);
      if (auto && test) {
        fail("Invalid robot state bits. Autonomous and test mode bits are both set");
      }
      if (auto) {
        assertEquals(Autonomous, controlState, "'Autonomous' flag is set, should be in autonomous mode");
      } else if (test) {
        assertEquals(Test, controlState, "'Test' flag is set, should be in test mode");
      } else {
        assertEquals(Teleoperated, controlState, "Neither Autonomous nor Test flag is set, should be in teleop mode");
      }
    } else {
      assertEquals(Disabled, controlState, "'Enabled' flag is not set, should be in disabled mode");
    }

    assertEquals(flagMatches(bits, ControlWord.EMERGENCY_STOP_FLAG), controlWord.isEmergencyStopped());
    assertEquals(flagMatches(bits, ControlWord.FMS_ATTACHED_FLAG), controlWord.isFmsAttached());
    assertEquals(flagMatches(bits, ControlWord.DS_ATTACHED_FLAG), controlWord.isDsAttached());
  }

  private static Stream<Arguments> createBitArgs() {
    // Skip bitfields with both AUTO and TEST flags set
    IntPredicate isValidState = i -> !flagMatches(i, ControlWord.AUTO_FLAG | ControlWord.TEST_FLAG);
    return IntStream.range(0, 64)
        .filter(isValidState)
        .mapToObj(Arguments::of);
  }

}
