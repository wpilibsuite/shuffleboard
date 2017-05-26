package edu.wpi.first.shuffleboard.dnd;

import javafx.scene.input.DataFormat;

/**
 * Utility class for the app's data formats.
 */
public final class DataFormats {

  /**
   * The prefix for all data formats specific to the app.
   */
  public static final String APP_PREFIX = "shuffleboard";

  /**
   * The data format for widgets being dragged.
   */
  public static final DataFormat widget = new DataFormat(APP_PREFIX + "/widget");

  /**
   * The data format for sources being dragged.
   */
  public static final DataFormat source = new DataFormat(APP_PREFIX + "/data-source");

  private DataFormats() {
  }

}
