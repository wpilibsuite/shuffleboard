package edu.wpi.first.shuffleboard.plugin.base.data.fms;

/**
 * Enumerates the possible states a robot can be in.
 */
public enum RobotControlState {

  /**
   * The robot is disabled. This can be because the match has not started, the match has ended, the robot has been
   * emergency stopped, the robot lost connection to the FMS, or the robot rebooted mid-match.
   */
  Disabled,

  /**
   * The robot is in autonomous mode and is controlling itself.
   */
  Autonomous,

  /**
   * The robot is in teleop mode and is being controlled by human operators.
   */
  Teleoperated,

  /**
   * The robot is in test (aka LiveWindow) mode.
   */
  Test

}
