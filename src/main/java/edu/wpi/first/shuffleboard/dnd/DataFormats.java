package edu.wpi.first.shuffleboard.dnd;

import edu.wpi.first.shuffleboard.util.GridPoint;

import java.io.Serializable;

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

  /**
   * Holds the information about a dragged widget.
   */
  public static final class WidgetData implements Serializable {

    private final String id;
    private final GridPoint dragPoint;

    /**
     * Creates a new widget data object with the given ID and initial drag point.
     *
     * @param id        the ID of the dragged widget
     * @param dragPoint the point the widget was dragged from
     */
    public WidgetData(String id, GridPoint dragPoint) {
      this.id = id;
      this.dragPoint = dragPoint;
    }

    public String getId() {
      return id;
    }

    public GridPoint getDragPoint() {
      return dragPoint;
    }
  }

}
