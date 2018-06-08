package edu.wpi.first.shuffleboard.api.dnd;

import edu.wpi.first.shuffleboard.api.util.GridPoint;

import java.io.Serializable;
import java.util.Set;
import java.util.UUID;

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
  public static final DataFormat singleTile = new DataFormat(APP_PREFIX + "/single-tile");

  /**
   * The data format for multiple tiles being dragged at once.
   */
  public static final DataFormat multipleTiles = new DataFormat(APP_PREFIX + "/multiple-tiles");

  /**
   * The data format for sources being dragged.
   */
  public static final DataFormat source = new DataFormat(APP_PREFIX + "/data-source");

  /**
   * The data format for widget type names (string).
   */
  public static final DataFormat widgetType = new DataFormat(APP_PREFIX + "/widget-type");

  /**
   * The data format for components that do not exist inside a tile.
   */
  public static final DataFormat tilelessComponent = new DataFormat(APP_PREFIX + "/tileless-component");

  private DataFormats() {
  }

  /**
   * Holds information about multiple dragged tiles.
   */
  public static final class MultipleTileData implements Serializable {
    private final Set<String> tileIds;
    private final GridPoint initialPoint;

    public MultipleTileData(Set<String> tileIds, GridPoint initialPoint) {
      this.tileIds = tileIds;
      this.initialPoint = initialPoint;
    }

    public Set<String> getTileIds() {
      return tileIds;
    }

    public GridPoint getInitialPoint() {
      return initialPoint;
    }
  }

  /**
   * Holds the information about a single dragged tile.
   */
  public static final class TileData implements Serializable {

    private final String id;
    private final GridPoint localDragPoint;

    /**
     * Creates a new tile data object with the given ID and initial drag point.
     *
     * @param id             the ID of the dragged widget
     * @param localDragPoint the point the tile was dragged from, local to that tile
     */
    public TileData(String id, GridPoint localDragPoint) {
      this.id = id;
      this.localDragPoint = localDragPoint;
    }

    public String getId() {
      return id;
    }

    public GridPoint getLocalDragPoint() {
      return localDragPoint;
    }
  }

  /**
   * Holds the information about a tileless component.
   */
  public static final class TilelessComponentData implements Serializable {

    private final UUID parentId;
    private final UUID componentId;

    public TilelessComponentData(UUID parentId, UUID componentId) {
      this.parentId = parentId;
      this.componentId = componentId;
    }

    public UUID getParentId() {
      return parentId;
    }

    public UUID getComponentId() {
      return componentId;
    }
  }

}
