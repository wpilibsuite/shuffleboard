package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;

import java.util.Objects;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;

/**
 * A helper class for selecting tiles in a {@link WidgetPane}.
 */
public final class TileSelector {

  private final WidgetPane pane;
  private final Pane dragHighlightContainer = new Pane();

  private boolean dragSelection = false;
  private Point2D dragStart = null;
  private Rectangle dragArea;

  private final ObservableSet<Tile<?>> selectedTiles = FXCollections.observableSet();

  /**
   * Creates a new tile selector.
   */
  public TileSelector(WidgetPane pane) {
    this.pane = Objects.requireNonNull(pane, "Pane cannot be null");

    pane.getChildren().add(0, dragHighlightContainer);
    dragHighlightContainer.setStyle("-fx-background-color: transparent;");

    setupMultiselectDrag();
    selectedTiles.addListener(TileSelector::updateTileState);
  }

  private static void updateTileState(SetChangeListener.Change<? extends Tile<?>> change) {
    if (change.wasAdded()) {
      change.getElementAdded().setSelected(true);
    } else {
      change.getElementRemoved().setSelected(false);
    }
  }

  private void setupMultiselectDrag() {
    pane.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
      Optional<Tile> clickedTile = pane.getTiles().stream()
          .filter(t -> t.getBoundsInLocal().contains(t.sceneToLocal(e.getSceneX(), e.getSceneY())))
          .findFirst();
      dragSelection = !clickedTile.isPresent();
      if (dragSelection) {
        dragStart = new Point2D(e.getX(), e.getY());
      } else {
        dragStart = null;
      }
    });

    pane.addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
      if (dragSelection) {
        double minX = Math.min(dragStart.getX(), e.getX());
        double minY = Math.min(dragStart.getY(), e.getY());
        double maxX = Math.max(dragStart.getX(), e.getX());
        double maxY = Math.max(dragStart.getY(), e.getY());
        dragHighlightContainer.getChildren().remove(dragArea);
        dragArea = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        dragArea.getStyleClass().add("grid-selection");
        dragHighlightContainer.getChildren().add(dragArea);
        dragHighlightContainer.toFront();
        updateSelections();
      }
    });

    pane.addEventHandler(MouseEvent.MOUSE_RELEASED, __ -> {
      if (dragSelection) {
        updateSelections();
        dragHighlightContainer.getChildren().remove(dragArea);
        dragSelection = false;
        dragArea = null;
        dragStart = null;
        dragHighlightContainer.toBack();
      }
    });
  }

  /**
   * Updates the set of selected tiles.  Tiles are selected if the drag area contains or intersects the bounds of the
   * tile.
   */
  private void updateSelections() {
    if (dragSelection && dragArea != null) {
      Bounds dragBounds = dragArea.localToScene(dragArea.getBoundsInLocal());
      for (Tile<?> tile : pane.getTiles()) {
        boolean intersects = tile.localToScene(tile.getBoundsInLocal()).intersects(dragBounds);
        tile.setSelected(intersects);
        if (intersects) {
          select(tile);
        }
      }
    } else {
      selectedTiles.clear();
    }
  }

  /**
   * Gets the set of selected tiles.
   */
  public ObservableSet<Tile<?>> getSelectedTiles() {
    return selectedTiles;
  }

  /**
   * Checks if a tile is selected.
   *
   * @param tile the tile to check
   *
   * @return true if the tile is selected, false if it is not
   */
  public boolean isSelected(Tile<?> tile) {
    return selectedTiles.contains(tile);
  }

  public boolean areTilesSelected() {
    return !selectedTiles.isEmpty();
  }

  /**
   * Deselects all tiles.
   */
  public void deselectAll() {
    selectedTiles.clear();
  }

  /**
   * Manually selects a specific tile.
   *
   * @param tile the tile to select
   */
  public void select(Tile<?> tile) {
    selectedTiles.add(tile);
  }

  /**
   * Manually deselects a specific tile.
   *
   * @param tile the tile to deselect
   */
  public void deselect(Tile<?> tile) {
    selectedTiles.remove(tile);
  }

  /**
   * Toggles the selection state of a tile.
   *
   * @param tile the tile to toggle.
   */
  public void toggleSelect(Tile<?> tile) {
    if (isSelected(tile)) {
      deselect(tile);
    } else {
      select(tile);
    }
  }

}
