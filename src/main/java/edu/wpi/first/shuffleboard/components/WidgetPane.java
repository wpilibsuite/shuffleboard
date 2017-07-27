package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.dnd.DragUtils;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.util.GridPoint;
import edu.wpi.first.shuffleboard.widget.TileSize;
import edu.wpi.first.shuffleboard.widget.Widget;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import org.fxmisc.easybind.EasyBind;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A type of tile pane specifically for widgets.
 */
public class WidgetPane extends TilePane {

  private final ObservableList<WidgetTile> tiles;
  private final Pane gridHighlight = new StackPane();

  private final BooleanProperty highlight
      = new SimpleBooleanProperty(this, "highlight", false);
  private final Property<GridPoint> highlightPoint
      = new SimpleObjectProperty<>(this, "highlightPoint", null);
  private final Property<TileSize> highlightSize
      = new SimpleObjectProperty<>(this, "highlightSize", null);

  /**
   * Creates a new widget pane. This sets up everything needed for dragging widgets and sources
   * around in this pane.
   */
  public WidgetPane() {
    gridHighlight.getStyleClass().add("grid-highlight");

    tiles = EasyBind.map(getChildren().filtered(n -> n instanceof WidgetTile), n -> (WidgetTile) n);

    // Add the highlighter when we're told to highlight
    highlight.addListener((__, old, highlight) -> {
      if (highlight) {
        getChildren().add(gridHighlight);
        gridHighlight.toFront();
      } else {
        getChildren().remove(gridHighlight);
      }
    });

    // Move the highlighter when the location changes
    highlightPoint.addListener((__, old, point) -> {
      if (getHighlightSize() == null || point == null) {
        return;
      }
      gridHighlight.toFront();
      moveNode(gridHighlight, point);
      gridHighlight.pseudoClassStateChanged(
          PseudoClass.getPseudoClass("colliding"),
          !isOpen(point, getHighlightSize(), DragUtils.isDraggedWidget));
    });

    // Resize the highlighter then when the size changes
    highlightSize.addListener((__, old, size) -> {
      if (getHighlightPoint() == null || size == null) {
        return;
      }
      gridHighlight.toFront();
      setSize(gridHighlight, size);
      gridHighlight.pseudoClassStateChanged(
          PseudoClass.getPseudoClass("colliding"),
          !isOpen(getHighlightPoint(), size, DragUtils.isDraggedWidget));
    });

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("WidgetPane.fxml"));
    fxmlLoader.setRoot(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new IllegalStateException("Can't load FXML : " + getClass().getSimpleName(), e);
    }
  }


  public ObservableList<WidgetTile> getTiles() {
    return tiles;
  }

  /**
   * Gets the first widget tile that matches the given predicate.
   *
   * @param predicate the predicate to use to find the desired widget tile
   */
  public Optional<WidgetTile> tileMatching(Predicate<WidgetTile> predicate) {
    return tiles.stream()
                .filter(predicate)
                .findFirst();
  }

  /**
   * Gets the tile for the widget containing the given source.
   */
  public Optional<WidgetTile> widgetForSource(DataSource<?> source) {
    return tileMatching(tile -> tile.getWidget().getSource() == source);
  }

  /**
   * Adds a widget to the tile view in the first available location.
   *
   * @param widget the widget to add
   */
  public WidgetTile addWidget(Widget widget) {
    TileSize size = sizeOfWidget(widget);
    return addWidget(widget, size);
  }

  /**
   * Adds a widget to the tile view in the first available location. The tile will be the specified
   * size.
   *
   * @param widget the widget to add
   * @param size   the size of the tile used to display the widget
   */
  public WidgetTile addWidget(Widget widget, TileSize size) {
    WidgetTile tile = new WidgetTile(widget, size);
    tile.sizeProperty().addListener(__ -> setSize(tile, tile.getSize()));
    addTile(tile, size);
    return tile;
  }

  /**
   * Adds a widget to the tile view in the specified location. The tile will be the specified
   * size.
   *
   * @param widget the widget to add
   * @param size   the size of the tile used to display the widget
   */
  public WidgetTile addWidget(Widget widget, GridPoint location, TileSize size) {
    WidgetTile tile = new WidgetTile(widget, size);
    tile.sizeProperty().addListener(__ -> setSize(tile, tile.getSize()));
    addTile(tile, location, size);
    return tile;
  }

  /**
   * Sets the size of a widget tile in this pane. This calls {@link #setSize(Node, TileSize)} as well as setting the
   * minimum and maximum size of the tile to ensure that it aligns properly with the grid.
   *
   * @param tile the tile to resize
   * @param size the new size of the tile
   */
  public void setSize(WidgetTile tile, TileSize size) {
    super.setSize(tile, size);
    tile.setMinWidth(tileSizeToWidth(size.getWidth()));
    tile.setMinHeight(tileSizeToHeight(size.getHeight()));
    tile.setMaxWidth(tileSizeToWidth(size.getWidth()));
    tile.setMaxHeight(tileSizeToHeight(size.getHeight()));
  }

  /**
   * @return Returns the expected size of the widget, in tiles.
   */
  public TileSize sizeOfWidget(Widget widget) {
    Pane view = widget.getView();
    double width = Math.max(getTileSize(), view.getPrefWidth());
    double height = Math.max(getTileSize(), view.getPrefHeight());

    return new TileSize((int) (width / getTileSize()),
            (int) (height / getTileSize()));
  }

  public void removeWidget(WidgetTile tile) {
    getChildren().remove(tile);
  }

  /**
   * Gets the tile at the given point in the grid.
   */
  public Optional<WidgetTile> tileAt(GridPoint point) {
    return tileAt(point.col, point.row);
  }

  /**
   * Gets the tile at the given point in the grid.
   *
   * @param col the column of the point to check
   * @param row the row of the point to check
   */
  public Optional<WidgetTile> tileAt(int col, int row) {
    return tiles.stream()
        .filter(tile -> GridPane.getColumnIndex(tile) <= col
            && GridPane.getColumnIndex(tile) + tile.getSize().getWidth() > col)
        .filter(tile -> GridPane.getRowIndex(tile) <= row
            && GridPane.getRowIndex(tile) + tile.getSize().getHeight() > row)
        .findAny();
  }

  @Override
  public boolean isOpen(int col, int row, int tileWidth, int tileHeight, Predicate<Node> ignore) {
    // overload to also ignore the highlight (it's not an actual tile)
    return super.isOpen(col, row, tileWidth, tileHeight, ignore.or(n -> n == gridHighlight));
  }

  public final boolean getHighlight() {
    return highlight.get();
  }

  public final BooleanProperty highlightProperty() {
    return highlight;
  }

  /**
   * Sets whether or not a section of the grid should be highlighted.
   */
  public final void setHighlight(boolean highlight) {
    this.highlight.set(highlight);
  }

  public final GridPoint getHighlightPoint() {
    return highlightPoint.getValue();
  }

  public final Property<GridPoint> highlightPointProperty() {
    return highlightPoint;
  }

  /**
   * Sets the origin point of the section of the grid to be highlighted.
   */
  public final void setHighlightPoint(GridPoint highlightPoint) {
    this.highlightPoint.setValue(highlightPoint);
  }

  public final TileSize getHighlightSize() {
    return highlightSize.getValue();
  }

  public final Property<TileSize> highlightSizeProperty() {
    return highlightSize;
  }

  /**
   * Sets the size of the section of the grid to be highlighted.
   */
  public final void setHighlightSize(TileSize highlightSize) {
    this.highlightSize.setValue(highlightSize);
  }

  /**
   * Will select the widgets that match predicate, and de-select all other widgets.
   */
  public void selectWidgets(Predicate<Widget> predicate) {
    tiles.forEach(tile -> tile.setSelected(predicate.test(tile.getWidget())));
  }
}
