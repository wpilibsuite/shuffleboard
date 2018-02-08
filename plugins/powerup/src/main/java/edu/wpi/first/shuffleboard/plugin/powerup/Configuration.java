package edu.wpi.first.shuffleboard.plugin.powerup;

/**
 * Possible configurations of the scale and the switches.
 */
public enum Configuration {

  /**
   * Unknown configuration. The FMS most likely has not set the values.
   */
  UNKNOWN,

  /**
   * The red PLATE is on the left-hand side of the field element as seen from the current alliance station.
   */
  RED_LEFT,

  /**
   * The red PLATE is on the right-hand side of the field element as seen from the current alliance station.
   */
  RED_RIGHT

}
