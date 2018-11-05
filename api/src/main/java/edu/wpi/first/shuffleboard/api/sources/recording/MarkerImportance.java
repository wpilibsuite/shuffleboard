package edu.wpi.first.shuffleboard.api.sources.recording;

/**
 * The levels of importance of a marker.
 */
public enum MarkerImportance {
  /**
   * The lowest importance level. This can be used for marking the most minor events that are nice to know if something
   * goes wrong, but otherwise has no use.
   */
  NONE,
  /**
   * Marks an event with low importance.
   */
  LOW,
  /**
   * Marks an event with no special connotations, such as the start of a match or match period, or scoring a game piece.
   */
  NORMAL,
  /**
   * Marks an important event.
   */
  HIGH,
  /**
   * Marks a critical event such as a component failure or robot reboot.
   */
  CRITICAL;

  /**
   * Identical to {@code values()[ordinal]}, but without the array copy.
   *
   * @param ordinal the ordinal of the enum constant to get
   *
   * @return the enum constant with the given ordinal
   */
  public static MarkerImportance valueOf(int ordinal) {
    switch (ordinal) {
      case 0:
        return NONE;
      case 1:
        return LOW;
      case 2:
        return NORMAL;
      case 3:
        return HIGH;
      case 4:
        return CRITICAL;
      default:
        throw new IllegalArgumentException("Ordinal out of range: " + ordinal);
    }
  }

}
