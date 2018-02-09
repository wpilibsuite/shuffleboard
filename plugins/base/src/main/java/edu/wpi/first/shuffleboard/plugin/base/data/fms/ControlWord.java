package edu.wpi.first.shuffleboard.plugin.base.data.fms;

import com.google.common.annotations.VisibleForTesting;

import static edu.wpi.first.shuffleboard.api.util.BitUtils.flagMatches;
import static edu.wpi.first.shuffleboard.api.util.BitUtils.toFlag;

/**
 * Holds miscellaneous information about the state of the robot program and its connections to the FMS.
 */
public final class ControlWord {

  @VisibleForTesting static final int ENABLED_FLAG = 0x01;
  @VisibleForTesting static final int AUTO_FLAG = 0x02;
  @VisibleForTesting static final int TEST_FLAG = 0x04;
  @VisibleForTesting static final int EMERGENCY_STOP_FLAG = 0x08;
  @VisibleForTesting static final int FMS_ATTACHED_FLAG = 0x10;
  @VisibleForTesting static final int DS_ATTACHED_FLAG = 0x20;

  private final RobotControlState controlState;
  private final boolean emergencyStopped;
  private final boolean fmsAttached;
  private final boolean dsAttached;

  /**
   * Creates a new ControlWord object.
   *
   * @param controlState     the state of the robot
   * @param emergencyStopped if the robot is E-stopped
   * @param fmsAttached      if the robot has a connection to the FMS
   * @param dsAttached       if the robot has a connection to the FRC DriverStation
   */
  public ControlWord(RobotControlState controlState,
                     boolean emergencyStopped,
                     boolean fmsAttached,
                     boolean dsAttached) {
    this.controlState = controlState;
    this.emergencyStopped = emergencyStopped;
    this.fmsAttached = fmsAttached;
    this.dsAttached = dsAttached;
  }

  /**
   * Creates a new ControlWord object from a control word bitfield.
   *
   * @param word the control word bitfield
   */
  public static ControlWord fromBits(int word) {
    RobotControlState state;
    if (flagMatches(word, ENABLED_FLAG)) {
      if (flagMatches(word, TEST_FLAG)) {
        state = RobotControlState.Test;
      } else if (flagMatches(word, AUTO_FLAG)) {
        state = RobotControlState.Autonomous;
      } else {
        state = RobotControlState.Teleoperated;
      }
    } else {
      state = RobotControlState.Disabled;
    }
    return new ControlWord(state,
        flagMatches(word, EMERGENCY_STOP_FLAG),
        flagMatches(word, FMS_ATTACHED_FLAG),
        flagMatches(word, DS_ATTACHED_FLAG)
    );
  }

  /**
   * Converts this ControlWord to bits that WPILib can understand.
   */
  public int toBits() {
    return toFlag(controlState != RobotControlState.Disabled, ENABLED_FLAG)
        | toFlag(controlState == RobotControlState.Autonomous, AUTO_FLAG)
        | toFlag(controlState == RobotControlState.Test, TEST_FLAG)
        | toFlag(emergencyStopped, EMERGENCY_STOP_FLAG)
        | toFlag(fmsAttached, FMS_ATTACHED_FLAG)
        | toFlag(dsAttached, DS_ATTACHED_FLAG);
  }

  /**
   * Gets the control state of the robot.
   */
  public RobotControlState getControlState() {
    return controlState;
  }

  /**
   * Checks if the robot is E-stopped.
   */
  public boolean isEmergencyStopped() {
    return emergencyStopped;
  }

  /**
   * Checks if the robot has a connection to the FMS.
   */
  public boolean isFmsAttached() {
    return fmsAttached;
  }

  /**
   * Checks if the robot has a connection to the FRC DriverStation.
   */
  public boolean isDsAttached() {
    return dsAttached;
  }

}
