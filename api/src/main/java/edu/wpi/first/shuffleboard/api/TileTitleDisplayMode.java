package edu.wpi.first.shuffleboard.api;

public enum TileTitleDisplayMode {
  /** The default tile view type - large title ad header bar. */
  DEFAULT("Default"),
  /** Minimal tiles with smaller header bars and titles. */
  MINIMAL("Minimal"),
  /** No header bars at all. */
  HIDDEN("Hidden");

  private final String humanReadable;

  TileTitleDisplayMode(String humanReadable) {
    this.humanReadable = humanReadable;
  }

  @Override
  public String toString() {
    return humanReadable;
  }
}
