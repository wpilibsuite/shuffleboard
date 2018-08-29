package edu.wpi.first.shuffleboard.api.sources.recording;

/**
 * The levels of importance of a marker.
 */
public enum MarkerImportance {
  LOWEST,
  LOW,
  NORMAL,
  HIGH,
  HIGHEST;

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
        return LOWEST;
      case 1:
        return LOW;
      case 2:
        return NORMAL;
      case 3:
        return HIGH;
      case 4:
        return HIGHEST;
      default:
        throw new IllegalArgumentException("Ordinal out of range: " + ordinal);
    }
  }

}
