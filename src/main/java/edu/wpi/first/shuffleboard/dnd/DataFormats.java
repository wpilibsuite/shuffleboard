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
  public static final DataFormat widgetTile = new DataFormat(APP_PREFIX + "/widgetTile");

  /**
   * The data format for sources being dragged.
   */
  public static final DataFormat source = new DataFormat(APP_PREFIX + "/data-source");

  /**
   * The data format for widget type names (string).
   */
  public static final DataFormat widgetType = new DataFormat(APP_PREFIX + "/widget-type");

  private DataFormats() {
  }

}
