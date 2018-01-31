package edu.wpi.first.shuffleboard.plugin.base.data.fms;

/**
 * Represents the types of FRC matches at events.
 */
public enum MatchType {

  /**
   * No match is being played.
   */
  NONE("Unknown"),

  /**
   * A practice match is being played.
   */
  PRACTICE("Practice"),

  /**
   * A qualification match is being played.
   */
  QUALIFICATION("Qualification"),

  /**
   * An elimination ("playoff") match is being played.
   */
  ELIMINATION("Elimination");

  private final String humanReadableName;

  MatchType(String humanReadableName) {
    this.humanReadableName = humanReadableName;
  }

  /**
   * Gets a human-readable name for this ordinal.
   */
  public String getHumanReadableName() {
    return humanReadableName;
  }

  /**
   * Gets the match type from its ordinal. If no match type is associated with the given ordinal, {@link #NONE} is
   * returned.
   *
   * @param ordinal the ordinal of the match type
   *
   * @return the match type with the given ordinal
   */
  public static MatchType fromOrdinal(int ordinal) {
    if (ordinal < 0 || ordinal > ELIMINATION.ordinal()) {
      return NONE;
    }
    return values()[ordinal];
  }

}
