package edu.wpi.first.shuffleboard.api.sources.recording;

/**
 * The levels of importance of a marker.
 */
public enum MarkerImportance {


  // Maintainer note: this enum is mirrored in WPILib. Adding or removing entries, or changing the name of any entry,
  // requires a corresponding change to WPILib.

  /**
   * The lowest importance level. This can be used for marking the most minor events that are nice to know if something
   * goes wrong, but otherwise has no use.
   */
  TRIVIAL(0),
  /**
   * Marks an event with low importance.
   */
  LOW(1),
  /**
   * Marks an event with no special connotations, such as the start of a match or match period, or scoring a game piece.
   */
  NORMAL(2),
  /**
   * Marks an important event.
   */
  HIGH(3),
  /**
   * Marks a critically important event such as a component failure, power loss, software deadlock, or timeout.
   */
  CRITICAL(4);

  private final int id;

  MarkerImportance(int id) {
    this.id = id;
  }

  /**
   * Gets the importance level with the given ID number.
   *
   * @param id the ID number of the enum constant to get
   *
   * @return the enum constant with the given ID
   */
  public static MarkerImportance forId(int id) {
    switch (id) {
      case 0:
        return TRIVIAL;
      case 1:
        return LOW;
      case 2:
        return NORMAL;
      case 3:
        return HIGH;
      case 4:
        return CRITICAL;
      default:
        throw new IllegalArgumentException("Unknown ID: " + id);
    }
  }

  public int getId() {
    return id;
  }
}
