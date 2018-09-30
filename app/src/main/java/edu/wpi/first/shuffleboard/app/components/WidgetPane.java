package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.css.SimpleColorCssMetaData;
import edu.wpi.first.shuffleboard.api.css.SimpleCssMetaData;
import edu.wpi.first.shuffleboard.api.util.GridImage;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.util.RoundingMode;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.ComponentContainer;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.dnd.DragUtils;
import edu.wpi.first.shuffleboard.app.dnd.ResizeUtils;
import edu.wpi.first.shuffleboard.app.dnd.TileDragResizer;

import com.google.common.collect.ImmutableList;

import org.fxmisc.easybind.EasyBind;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.StyleConverter;
import javafx.css.Styleable;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

/**
 * A type of tile pane specifically for widgets.
 */
@SuppressWarnings("PMD.GodClass") // There's just a bunch of properties here
public class WidgetPane extends TilePane implements ComponentContainer {

  private final ObservableList<Tile> tiles;
  private final Pane gridHighlight = new StackPane();

  private final BooleanProperty highlight
      = new SimpleBooleanProperty(this, "highlight", false);
  private final Property<GridPoint> highlightPoint
      = new SimpleObjectProperty<>(this, "highlightPoint", new GridPoint(0, 0));
  private final Property<TileSize> highlightSize
      = new SimpleObjectProperty<>(this, "highlightSize", new TileSize(1, 1));
  private final BooleanProperty showGrid
      = new SimpleBooleanProperty(this, "showGrid", true);
  private final IntegerProperty gridLineBorderThickness
      = new SimpleIntegerProperty(this, "gridLineBorderThickness", 2);
  private final IntegerProperty secondaryGridLineCount
      = new SimpleIntegerProperty(this, "secondaryGridLineCount", 3);
  private final IntegerProperty secondaryGridLineThickness
      = new SimpleIntegerProperty(this, "secondaryGridLineThickness", 1);
  private final Property<Color> gridLineColor
      = new SimpleObjectProperty<>(this, "gridLineColor", Color.TRANSPARENT);

  /**
   * Creates a new widget pane. This sets up everything needed for dragging widgets and sources
   * around in this pane.
   */
  public WidgetPane() {
    getStyleClass().add("widget-pane");
    gridHighlight.getStyleClass().add("grid-highlight");

    // Bind the background to show a grid matching the size of the tiles (if enabled via showGrid)
    backgroundProperty().bind(
        Bindings.createObjectBinding(
            this::createGridBackground,
            tileSizeProperty(),
            hgapProperty(),
            vgapProperty(),
            showGrid,
            gridLineBorderThickness,
            secondaryGridLineCount,
            secondaryGridLineThickness,
            gridLineColor
        )
    );

    tiles = EasyBind.map(getChildren().filtered(n -> n instanceof Tile), n -> (Tile) n);

    // Add the highlighter when we're told to highlight
    highlight.addListener((__, old, highlight) -> {
      if (highlight) {
        getChildren().add(gridHighlight);
        gridHighlight.toFront();
        gridHighlight.setMouseTransparent(true);
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
          !isOpen(point, getHighlightSize(), DragUtils.isDraggedWidget.or(ResizeUtils.isResizedTile)));
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
          !isOpen(getHighlightPoint(), size, DragUtils.isDraggedWidget.or(ResizeUtils.isResizedTile)));
    });

    tileSizeProperty().addListener((__, prev, cur) -> resizeTiles());
    hgapProperty().addListener((__, prev, cur) -> resizeTiles());
    vgapProperty().addListener((__, prev, cur) -> resizeTiles());

    FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("WidgetPane.fxml"));
    fxmlLoader.setRoot(this);

    try {
      fxmlLoader.load();
    } catch (IOException e) {
      throw new IllegalStateException("Can't load FXML : " + getClass().getSimpleName(), e);
    }
  }

  @Override
  public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
    List<CssMetaData<? extends Styleable, ?>> list = new ArrayList<>(super.getCssMetaData());
    list.addAll(WidgetPaneCss.STYLEABLES);
    return list;
  }

  private Image makeGridImage(Number tileSize, Number hgap, Number vgap) {
    GridImage gridImage = new GridImage(
        (int) (tileSize.doubleValue() + hgap.doubleValue()),
        (int) (tileSize.doubleValue() + vgap.doubleValue()),
        gridLineBorderThickness.get(),
        secondaryGridLineCount.get(), secondaryGridLineThickness.get()
    );
    return gridImage.getAsImage(gridLineColor.getValue());
  }

  private static BackgroundImage makeTiledBackgroundImage(Image image) {
    if (image == null) {
      return null;
    } else {
      return new BackgroundImage(image, null, null, null, null);
    }
  }

  public ObservableList<Tile> getTiles() {
    return tiles;
  }

  /**
   * Gets the first widget tile that matches the given predicate.
   *
   * @param predicate the predicate to use to find the desired widget tile
   */
  public Optional<Tile> tileMatching(Predicate<Tile> predicate) {
    return tiles.stream()
        .map(TypeUtils.optionalCast(Tile.class))
        .flatMap(TypeUtils.optionalStream())
        .filter(predicate)
        .findFirst();
  }

  /**
   * Resizes all tiles in this pane to reflect the current tile size, vgap, and hgap.
   */
  private void resizeTiles() {
    tiles.forEach(tile -> {
      tile.setMaxWidth(tileSizeToWidth(tile.getSize().getWidth()));
      tile.setMaxHeight(tileSizeToHeight(tile.getSize().getHeight()));
    });
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
    if (addTile(tile, size) != null) {
      // can't set the size if it wasn't actually added
      setSize(tile, size);
    }
    TileDragResizer.makeResizable(this, tile);
    return tile;
  }

  /**
   * Adds a component to a tile.
   *
   * @param component the component to add
   * @param <C>       the type of the component
   *
   * @return the tile containing the component, or null if no tile was added
   */
  public <C extends Component> Tile<C> addComponentToTile(C component) {
    addComponent(component);
    return getTiles().stream()
        .filter(t -> t.getContent() == component)
        .findFirst()
        .orElse(null);
  }

  @Override
  public void addComponent(Component component) {
    if (component instanceof Widget) {
      addWidget((Widget) component);
    } else {
      TileSize size = sizeOfWidget(component);
      GridPoint location = firstPoint(size.getWidth(), size.getHeight());
      if (location == null) {
        // Nowhere to place the component
        return;
      }
      Tile<?> tile = addComponent(component, location, size);
      if (getChildren().contains(tile)) {
        // Can only set the size if the tile was actually added
        TileDragResizer.makeResizable(this, tile);
        setSize(tile, size);
      }
    }
  }

  /**
   * Add an arbitrary component to the WidgetPane in the specified location.
   * The tile will be the specified size.
   *
   * @param component the component to add
   * @param size      the size of the tile used to display the component
   */
  public <C extends Component> Tile<C> addComponent(C component, GridPoint location, TileSize size) {
    Tile<C> tile = Tile.tileFor(component, size);
    TileDragResizer.makeResizable(this, tile);
    tile.sizeProperty().addListener(__ -> setSize(tile, tile.getSize()));
    addTile(tile, location, size);
    return tile;
  }

  /**
   * Add an arbitrary component to the WidgetPane in the specified location. The tile's size will be calculated with
   * {@link #sizeOfWidget(Component)}.
   *
   * @param component the component to add
   * @param location  the location of the component
   */
  public <C extends Component> Tile<C> addComponent(C component, GridPoint location) {
    return addComponent(component, location, sizeOfWidget(component));
  }

  /*
   * Checks if there is enough open space to add the given component.
   *
   * @param component the component to check
   *
   * @return true if there is enough open space to add the component, false if not
   */
  public boolean canAdd(Component component) {
    return firstPoint(sizeOfWidget(component)) != null;
  }

  @Override
  public void removeComponent(Component component) {
    tiles.stream()
        .filter(tile -> tile.getContent() == component)
        .findFirst()
        .ifPresent(tiles::remove);
  }

  @Override
  public Stream<Component> components() {
    return tiles.stream().map(Tile::getContent);
  }

  /**
   * Sets the size of a widget tile in this pane. This calls {@link #setSize(Node, TileSize)} as well as setting the
   * minimum and maximum size of the tile to ensure that it aligns properly with the grid.
   *
   * @param tile the tile to resize
   * @param size the new size of the tile
   */
  public void setSize(Tile tile, TileSize size) {
    super.setSize(tile, size);
    tile.setMinWidth(tileSizeToWidth(size.getWidth()));
    tile.setMinHeight(tileSizeToHeight(size.getHeight()));
    tile.setMaxWidth(tileSizeToWidth(size.getWidth()));
    tile.setMaxHeight(tileSizeToHeight(size.getHeight()));
  }

  /**
   * Get the expected size of the widget, in tiles.
   */
  public TileSize sizeOfWidget(Component widget) {
    Pane view = widget.getView();

    return new TileSize(
        roundWidthToNearestTile(Math.max(view.getMinWidth(), view.getPrefWidth()), RoundingMode.UP),
        roundHeightToNearestTile(Math.max(view.getMinHeight(), view.getPrefHeight()), RoundingMode.UP)
    );
  }

  /**
   * Remove a given tile from the widget pane.
   *
   * @return the content of the removed tile.
   */
  public <T extends Component> T removeTile(Tile<T> tile) {
    T content = tile.getContent();
    getChildren().remove(tile);
    tile.setContent(null);
    return content;
  }

  /**
   * Gets the tile at the given point in the grid.
   */
  public Optional<Tile> tileAt(GridPoint point) {
    return tileAt(point.col, point.row);
  }

  /**
   * Gets the tile at the given point in the grid.
   *
   * @param col the column of the point to check
   * @param row the row of the point to check
   */
  public Optional<Tile> tileAt(int col, int row) {
    return tiles.stream()
        .filter(tile -> getColumnIndex(tile) <= col
            && getColumnIndex(tile) + tile.getSize().getWidth() > col)
        .filter(tile -> getRowIndex(tile) <= row
            && getRowIndex(tile) + tile.getSize().getHeight() > row)
        .findAny();
  }

  @Override
  public boolean isOpen(int col, int row, int tileWidth, int tileHeight, Predicate<Node> ignore) {
    // overload to also ignore the highlight (it's not an actual tile)
    return super.isOpen(col, row, tileWidth, tileHeight, ignore.or(n -> n == gridHighlight));
  }

  public final boolean isHighlight() {
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
    tiles.filtered(t -> t instanceof WidgetTile)
        .forEach(tile -> tile.setSelected(
            predicate.test(((WidgetTile) tile).getContent())
        ));
  }

  public boolean isShowGrid() {
    return showGrid.get();
  }

  public BooleanProperty showGridProperty() {
    return showGrid;
  }

  public void setShowGrid(boolean showGrid) {
    this.showGrid.set(showGrid);
  }

  public int getGridLineBorderThickness() {
    return gridLineBorderThickness.get();
  }

  public IntegerProperty gridLineBorderThicknessProperty() {
    return gridLineBorderThickness;
  }

  public void setGridLineBorderThickness(int gridLineBorderThickness) {
    this.gridLineBorderThickness.set(gridLineBorderThickness);
  }

  public int getSecondaryGridLineCount() {
    return secondaryGridLineCount.get();
  }

  public IntegerProperty secondaryGridLineCountProperty() {
    return secondaryGridLineCount;
  }

  public void setSecondaryGridLineCount(int secondaryGridLineCount) {
    this.secondaryGridLineCount.set(secondaryGridLineCount);
  }

  public int getSecondaryGridLineThickness() {
    return secondaryGridLineThickness.get();
  }

  public IntegerProperty secondaryGridLineThicknessProperty() {
    return secondaryGridLineThickness;
  }

  public void setSecondaryGridLineThickness(int secondaryGridLineThickness) {
    this.secondaryGridLineThickness.set(secondaryGridLineThickness);
  }

  public Color getGridLineColor() {
    return gridLineColor.getValue();
  }

  public Property<Color> gridLineColorProperty() {
    return gridLineColor;
  }

  public void setGridLineColor(Color gridLineColor) {
    this.gridLineColor.setValue(gridLineColor);
  }

  private Background createGridBackground() {
    if (isShowGrid()) {
      return new Background(makeTiledBackgroundImage(makeGridImage(getTileSize(), getHgap(), getVgap())));
    } else {
      return null;
    }
  }

  /**
   * Creates a new highlight object and adds it to this pane at (0, 0) with size (1, 1). The location and size of the
   * highlight can be configured with {@link Highlight#withLocation} and {@link Highlight#withSize}, respectively.
   *
   * @return a new highlight object
   */
  public Highlight addHighlight() {
    Highlight highlight = new Highlight();
    getChildren().add(highlight);
    setSize(highlight, highlight.getSize());
    moveNode(highlight, highlight.getLocation());
    highlight.sizeProperty().addListener((__, old, size) -> setSize(highlight, size));
    highlight.locationProperty().addListener((__, old, location) -> moveNode(highlight, location));
    highlight.toFront();
    return highlight;
  }

  public void removeHighlight(Highlight highlight) {
    getChildren().remove(highlight);
  }

  public static final class Highlight extends Pane {

    private final ObjectProperty<TileSize> size =
        new SimpleObjectProperty<>(this, "size", new TileSize(1, 1));
    private final ObjectProperty<GridPoint> location =
        new SimpleObjectProperty<>(this, "location", new GridPoint(0, 0));

    Highlight() {
      getStyleClass().add("grid-highlight");
    }

    public Highlight withSize(TileSize size) {
      this.size.setValue(size);
      return this;
    }

    public Highlight withLocation(GridPoint location) {
      this.location.setValue(location);
      return this;
    }

    public TileSize getSize() {
      return size.get();
    }

    public GridPoint getLocation() {
      return location.get();
    }

    public ObjectProperty<TileSize> sizeProperty() {
      return size;
    }

    public ObjectProperty<GridPoint> locationProperty() {
      return location;
    }
  }

  private static final class WidgetPaneCss {

    private static final CssMetaData<WidgetPane, Color> GRID_LINE_COLOR =
        new SimpleColorCssMetaData<>(
            "-fx-grid-line-color",
            WidgetPane::gridLineColorProperty
        );
    private static final CssMetaData<WidgetPane, Number> GRID_LINE_BORDER_THICKNESS =
        new SimpleCssMetaData<>(
            "-fx-grid-line-border-thickness",
            StyleConverter.getSizeConverter(),
            WidgetPane::gridLineBorderThicknessProperty
        );
    private static final CssMetaData<WidgetPane, Number> SECONDARY_GRID_LINE_COUNT =
        new SimpleCssMetaData<>(
            "-fx-secondary-grid-line-count",
            StyleConverter.getSizeConverter(),
            WidgetPane::secondaryGridLineCountProperty
        );
    private static final CssMetaData<WidgetPane, Number> SECONDARY_GRID_LINE_THICKNESS =
        new SimpleCssMetaData<>(
            "-fx-secondary-grid-line-thickness",
            StyleConverter.getSizeConverter(),
            WidgetPane::secondaryGridLineThicknessProperty
        );

    private static final List<CssMetaData<WidgetPane, ?>> STYLEABLES = ImmutableList.of(
        GRID_LINE_COLOR,
        GRID_LINE_BORDER_THICKNESS,
        SECONDARY_GRID_LINE_COUNT,
        SECONDARY_GRID_LINE_THICKNESS
    );

  }

}
