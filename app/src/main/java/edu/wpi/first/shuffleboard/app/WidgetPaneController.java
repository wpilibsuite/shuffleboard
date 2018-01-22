package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.components.ExtendedPropertySheet;
import edu.wpi.first.shuffleboard.api.dnd.DataFormats;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.DummySource;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.util.RoundingMode;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.ComponentContainer;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.LayoutType;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.components.LayoutTile;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.TileLayout;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;
import edu.wpi.first.shuffleboard.app.components.WidgetTile;
import edu.wpi.first.shuffleboard.app.dnd.TileDragResizer;
import edu.wpi.first.shuffleboard.app.json.SourcedRestorer;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import org.fxmisc.easybind.EasyBind;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javafx.beans.binding.Binding;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

// needs refactoring to split out per-widget interaction
@SuppressWarnings("PMD.GodClass")
public class WidgetPaneController {

  private static final Logger log = Logger.getLogger(WidgetPaneController.class.getName());

  @FXML
  private WidgetPane pane;

  private final Map<Node, Boolean> tilesAlreadySetup = new WeakHashMap<>();

  /**
   * Memoizes the size of a tile that would be added when dropping a source or widget. Memoizing prevents calling
   * potentially expensive component initialization code every time the mouse moves when previewing the location of a
   * tile for a source or widget.
   */
  private TileSize tilePreviewSize = null;

  @FXML
  private void initialize() {

    pane.getTiles().addListener((ListChangeListener<Tile>) changes -> {
      while (changes.next()) {
        changes.getAddedSubList().forEach(this::setupTile);
      }
    });

    // Add a context menu for pane-related actions
    pane.setOnContextMenuRequested(this::createPaneContextMenu);

    // Handle being dragged over
    pane.setOnDragOver(event -> {
      event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
      GridPoint point = pane.pointAt(event.getX(), event.getY());
      boolean isWidgetTile = event.getDragboard().hasContent(DataFormats.widgetTile);
      boolean isSource = event.getDragboard().hasContent(DataFormats.source);
      boolean isWidget = event.getDragboard().hasContent(DataFormats.widgetType);

      // preview the location of the widget if one is being dragged
      if (isWidgetTile) {
        pane.setHighlight(true);
        pane.setHighlightPoint(point);
        DataFormats.WidgetData data = (DataFormats.WidgetData) event.getDragboard().getContent(DataFormats.widgetTile);
        pane.tileMatching(tile -> tile.getId().equals(data.getId()))
            .ifPresent(tile -> previewTile(tile, point.subtract(data.getDragPoint())));
      } else if (isSource) {
        if (!pane.isOpen(point, new TileSize(1, 1), n -> false)) {
          // Dragged a source onto a tile, let the tile handle the drag and drop
          pane.setHighlight(false);
          return;
        }
        SourceEntry entry = (SourceEntry) event.getDragboard().getContent(DataFormats.source);
        DataSource source = entry.get();
        Optional<String> componentName = Components.getDefault().pickComponentNameFor(source.getDataType());
        Optional<DataSource<?>> dummySource = DummySource.forTypes(source.getDataType());
        if (componentName.isPresent() && dummySource.isPresent()) {
          if (tilePreviewSize == null) {
            Components.getDefault().createComponent(componentName.get(), dummySource.get())
                .map(pane::sizeOfWidget)
                .ifPresent(size -> tilePreviewSize = size);
          }
          if (tilePreviewSize == null) {
            pane.setHighlight(false);
          } else {
            pane.setHighlight(true);
            pane.setHighlightPoint(point);
            pane.setHighlightSize(tilePreviewSize);
          }
        }
      } else if (isWidget) {
        if (!pane.isOpen(point, new TileSize(1, 1), n -> false)) {
          // Dragged a widget onto a tile, can't drop
          pane.setHighlight(false);
          return;
        }
        String componentType = (String) event.getDragboard().getContent(DataFormats.widgetType);
        if (tilePreviewSize == null) {
          Components.getDefault().createComponent(componentType)
              .map(pane::sizeOfWidget)
              .ifPresent(size -> tilePreviewSize = size);
        }
        if (tilePreviewSize == null) {
          pane.setHighlight(false);
        } else {
          pane.setHighlight(true);
          pane.setHighlightPoint(point);
          pane.setHighlightSize(tilePreviewSize);
        }
      }

      event.consume();
    });

    // Clean up widget drags when the drag exits the pane
    pane.setOnDragDone(__ -> cleanupWidgetDrag());
    pane.setOnDragExited(__ -> cleanupWidgetDrag());

    // Handle dropping stuff onto the pane
    pane.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      GridPoint point = pane.pointAt(event.getX(), event.getY());
      // Dropping a source from the sources tree
      if (dragboard.hasContent(DataFormats.source)) {
        SourceEntry entry = (SourceEntry) dragboard.getContent(DataFormats.source);
        dropSource(entry.get(), point);
      }

      // Dropping a tile onto the pane after moving it around
      if (dragboard.hasContent(DataFormats.widgetTile)) {
        DataFormats.WidgetData data = (DataFormats.WidgetData) dragboard.getContent(DataFormats.widgetTile);
        pane.tileMatching(tile -> tile.getId().equals(data.getId()))
            .ifPresent(tile -> moveTile(tile, point.subtract(data.getDragPoint())));
      }

      // Dropping a widget from the gallery
      if (dragboard.hasContent(DataFormats.widgetType)) {
        String componentType = (String) dragboard.getContent(DataFormats.widgetType);
        Components.getDefault().createComponent(componentType).ifPresent(c -> {
          TileSize size = pane.sizeOfWidget(c);
          if (pane.isOpen(point, size, _t -> false)) {
            Tile<?> tile = pane.addComponentToTile(c);
            moveTile(tile, point);
          }
        });
      }

      cleanupWidgetDrag();
      tilePreviewSize = null;
      event.consume();
    });

    pane.parentProperty().addListener((__, old, parent) -> {
      if (parent instanceof Region) {
        Region region = (Region) parent;
        Binding<Integer> colBinding =
            EasyBind.combine(region.widthProperty(), pane.hgapProperty(), pane.tileSizeProperty(),
                (width, gap, size) -> pane.roundWidthToNearestTile(width.doubleValue(), RoundingMode.DOWN))
                .map(numCols -> Math.max(1, numCols));
        Binding<Integer> rowBinding =
            EasyBind.combine(region.heightProperty(), pane.vgapProperty(), pane.tileSizeProperty(),
                (height, gap, size) -> pane.roundHeightToNearestTile(height.doubleValue(), RoundingMode.DOWN))
                .map(numRows -> Math.max(1, numRows));


        pane.numColumnsProperty().bind(colBinding);
        pane.numRowsProperty().bind(rowBinding);
      }
    });

    pane.numColumnsProperty().addListener((__, oldCount, newCount) -> {
      if (pane.getTiles().isEmpty()) {
        // No tiles, bail
        return;
      }
      if (newCount < oldCount) {
        // shift and shrink tiles to the left
        pane.getTiles().stream()
            .filter(tile -> {
              final TileLayout layout = pane.getTileLayout(tile);
              return layout.origin.col + layout.size.getWidth() > newCount;
            })
            .forEach(tile -> collapseTile(tile, oldCount - newCount, true));
      }
    });
    pane.numRowsProperty().addListener((__, oldCount, newCount) -> {
      if (pane.getTiles().isEmpty()) {
        return;
      }
      if (newCount < oldCount) {
        // shift and shrink tiles up
        pane.getTiles().stream()
            .filter(tile -> {
              final TileLayout layout = pane.getTileLayout(tile);
              return layout.origin.row + layout.size.getHeight() > newCount;
            })
            .forEach(tile -> collapseTile(tile, oldCount - newCount, false));
      }
    });

    // Handle restoring data sources after a widget is added from a save file before its source(s) are available
    SourceTypes.getDefault().allAvailableSourceUris().addListener((ListChangeListener<String>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          SourcedRestorer restorer = new SourcedRestorer();
          // Restore sources for top-level Sourced objects
          pane.getTiles().stream()
              .map(Tile::getContent)
              .flatMap(TypeUtils.castStream(Sourced.class))
              .forEach(sourced -> restorer.restoreSourcesFor(
                  sourced,
                  c.getAddedSubList(),
                  WidgetPaneController::destroyedSourceCouldNotBeRestored));

          // Restore sources for all nested Sourced objects
          pane.getTiles().stream()
              .map(Tile::getContent)
              .flatMap(TypeUtils.castStream(ComponentContainer.class))
              .flatMap(ComponentContainer::allComponents)
              .flatMap(TypeUtils.castStream(Sourced.class))
              .forEach(sourced -> restorer.restoreSourcesFor(
                  sourced,
                  c.getAddedSubList(),
                  WidgetPaneController::destroyedSourceCouldNotBeRestored));
        } else if (c.wasRemoved()) {
          // Replace all removed sources with DestroyedSources
          pane.getTiles().stream()
              .map(Tile::getContent)
              .flatMap(Component::allComponents)
              .flatMap(TypeUtils.castStream(Sourced.class))
              .forEach(s -> replaceWithDestroyedSource(s, c.getRemoved()));
        }
      }
    });
  }

  private void createPaneContextMenu(ContextMenuEvent e) {
    MenuItem clear = FxUtils.menuItem("Clear", __ -> {
      List<Tile> tiles = new ArrayList<>(pane.getTiles());
      tiles.stream()
          .map((Function<Tile, Component>) pane::removeTile)
          .flatMap(Component::allComponents)
          .flatMap(TypeUtils.castStream(Sourced.class))
          .forEach(Sourced::removeAllSources);
    });
    ContextMenu contextMenu = new ContextMenu(clear);
    contextMenu.show(pane.getScene().getWindow(), e.getScreenX(), e.getScreenY());
  }

  /**
   * Replaces removed sources with destroyed versions that can be restored later if they become
   * available again.
   *
   * @param removedUris the URIs of the sources that were removed and should be replaced
   */
  private void replaceWithDestroyedSource(Sourced sourced, Collection<? extends String> removedUris) {
    sourced.getSources().replaceAll(source -> {
      if (source instanceof DestroyedSource) {
        return source;
      } else {
        if (removedUris.contains(source.getId())) {
          // Source is no longer available, replace with a destroyed source
          return DestroyedSource.forUnknownData(sourced.getDataTypes(), source.getId());
        } else {
          return source;
        }
      }
    });
  }

  /**
   * Cleans up from dragging widgets around in the tile pane.
   */
  private void cleanupWidgetDrag() {
    pane.setHighlight(false);
  }

  /**
   * Starts the drag of the given widget tile.
   */
  private void dragWidget(Tile tile, GridPoint point) {
    Dragboard dragboard = tile.startDragAndDrop(TransferMode.MOVE);
    WritableImage preview =
        new WritableImage(
            (int) tile.getBoundsInParent().getWidth(),
            (int) tile.getBoundsInParent().getHeight()
        );
    SnapshotParameters parameters = new SnapshotParameters();
    parameters.setFill(Color.TRANSPARENT);
    tile.snapshot(parameters, preview);
    dragboard.setDragView(preview);
    ClipboardContent content = new ClipboardContent();
    content.put(DataFormats.widgetTile, new DataFormats.WidgetData(tile.getId(), point));
    dragboard.setContent(content);
  }

  /**
   * Drops a widget into this pane at the given point.
   *
   * @param tile  the tile for the widget to drop
   * @param point the point in the tile pane to drop the widget at
   */
  private void moveTile(Tile tile, GridPoint point) {
    TileSize size = tile.getSize();
    if (pane.isOpen(point, size, n -> n == tile)) {
      pane.moveNode(tile, point);
    }
  }

  /**
   * Drops a data source into the tile pane at the given point. The source will be displayed
   * with the default widget for its data type. If there is no default widget for that data,
   * then no widget will be created.
   *
   * @param source the source to drop
   * @param point  the point to place the widget for the source
   */
  private void dropSource(DataSource<?> source, GridPoint point) {
    Components.getDefault().pickComponentNameFor(source.getDataType())
           .flatMap(name -> Components.getDefault().createComponent(name, source))
           .filter(widget -> pane.isOpen(point, pane.sizeOfWidget(widget), n -> widget == n))
           .map(pane::addComponentToTile)
           .ifPresent(tile -> pane.moveNode(tile, point));
  }

  /**
   * Sets up a tile with a context menu and lets it be dragged around.
   */
  private void setupTile(Tile tile) {
    if (tilesAlreadySetup.get(tile) != null) {
      return;
    }
    tilesAlreadySetup.put(tile, true);

    tile.setOnContextMenuRequested(event -> {
      ContextMenu contextMenu = createContextMenu(event);
      contextMenu.show(pane.getScene().getWindow(), event.getScreenX(), event.getScreenY());
      event.consume();
    });

    ActionList.registerSupplier(tile, () -> {
      ActionList widgetPaneActions = ActionList
          .withName(tile.getContent().getTitle())
          .addAction("Remove", () -> {
            Component removed = pane.removeTile(tile);
            if (removed instanceof Sourced) {
              ((Sourced) removed).removeAllSources();
            }
            if (removed instanceof Layout) {
              ((Layout) removed).allComponents()
                  .flatMap(TypeUtils.castStream(Sourced.class))
                  .forEach(Sourced::removeAllSources);
            }
          })
          .addNested(createLayoutMenus(tile));

      if (tile instanceof WidgetTile) {
        WidgetTile widgetTile = (WidgetTile) tile;
        ActionList changeMenus = createChangeMenusForWidget(widgetTile);
        if (changeMenus.hasItems()) {
          widgetPaneActions.addNested(changeMenus);
        }
        widgetPaneActions.addAction("Edit Properties",
            () -> showPropertySheet(widgetTile));
      }
      return widgetPaneActions;
    });

    TileDragResizer resizer = TileDragResizer.makeResizable(pane, tile);

    tile.setOnDragDetected(event -> {
      if (resizer.isDragging()) {
        // don't drag the widget while it's being resized
        return;
      }
      GridPoint dragPoint = new GridPoint(pane.roundWidthToNearestTile(event.getX()) - 1,
          pane.roundHeightToNearestTile(event.getY()) - 1);
      dragWidget(tile, dragPoint);
      event.consume();
    });

    tile.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      // Dragging a source onto a tile
      if (dragboard.hasContent(DataFormats.source) && tile.getContent() instanceof Sourced) {
        SourceEntry entry = (SourceEntry) dragboard.getContent(DataFormats.source);
        DataSource source = entry.get();
        Sourced sourced = (Sourced) tile.getContent();
        sourced.addSource(source);
        event.consume();

        return;
      }

      // Moving a layout tile around
      if (dragboard.hasContent(DataFormats.widgetTile) && tile instanceof LayoutTile) {
        DataFormats.WidgetData data = (DataFormats.WidgetData) event.getDragboard().getContent(DataFormats.widgetTile);

        if (tile.getId().equals(data.getId())) {
          return;
        }
        pane.tileMatching(t -> t.getId().equals(data.getId()))
            .ifPresent(t -> {
              Component content = pane.removeTile(t);
              ((LayoutTile) tile).getContent().addChild(content);
            });
        event.consume();

        return;
      }

      // Dragging a widget from the gallery
      if (dragboard.hasContent(DataFormats.widgetType) && tile instanceof LayoutTile) {
        String widgetType = (String) dragboard.getContent(DataFormats.widgetType);

        Components.getDefault().createWidget(widgetType).ifPresent(widget -> {
          ((LayoutTile) tile).getContent().addChild(widget);
        });
        event.consume();

        return;
      }

      // Dragging a source from the sources tree
      if (dragboard.hasContent(DataFormats.source) && tile instanceof LayoutTile) {
        SourceEntry entry = (SourceEntry) dragboard.getContent(DataFormats.source);

        Layout container = ((LayoutTile) tile).getContent();
        DataSource<?> source = entry.get();
        Components.getDefault().componentNamesForSource(entry.get())
            .stream()
            .findAny()
            .flatMap(name -> Components.getDefault().createWidget(name, source))
            .ifPresent(container::addChild);
        event.consume();

        return;
      }
    });
  }

  /**
   * Previews the widget for the given tile.
   *
   * @param tile  the tile for the widget to preview
   * @param point the point to preview the widget at
   */
  private void previewTile(Tile tile, GridPoint point) {
    TileSize size = tile.getSize();
    pane.setHighlightPoint(point);
    pane.setHighlightSize(size);
  }

  /**
   * Creates the context menu for a given tile.
   */
  private ContextMenu createContextMenu(ContextMenuEvent event) {
    ContextMenu menu = new ContextMenu();

    LinkedHashMap<String, List<MenuItem>> actions = new LinkedHashMap<>();
    if (event.getTarget() instanceof Node) {
      Node leaf = (Node) event.getTarget();
      Stream
          .iterate(leaf, Node::getParent)
          // non-functional ugliness necessary due to the lack of takeWhile in java 8
          .peek(node -> ActionList.actionsForNode(node).ifPresent(al -> {
            if (actions.containsKey(al.getName())) {
              actions.get(al.getName()).addAll(al.toMenuItems());
            } else {
              actions.put(al.getName(), al.toMenuItems());
            }
          }))
          .allMatch(n -> n.getParent() != null); // terminates infinite Stream#iterate
    }

    actions.forEach((key, menuItems) -> {
      menu.getItems().add(new SeparatorMenuItem());
      menu.getItems().add(FxUtils.menuLabel(key));
      menu.getItems().addAll(menuItems);
    });

    // remove leading separator.
    menu.getItems().remove(0);

    return menu;
  }

  /**
   * Creates all the menus needed for wrapping a component in a layout.
   *
   * @param tile the tile for the component to create the menus for
   */
  private ActionList createLayoutMenus(Tile<?> tile) {
    ActionList list = ActionList.withName("Add to new layout...");

    Components.getDefault()
        .allComponents()
        .flatMap(TypeUtils.castStream(LayoutType.class))
        .map(t -> (LayoutType<?>) t)
        .forEach(layoutType -> {
          list.addAction(layoutType.getName(), () -> {
            TileLayout was = pane.getTileLayout(tile);
            Component content = pane.removeTile(tile);
            Layout layout = layoutType.get();
            layout.addChild(content);
            pane.addComponent(layout, was.origin, was.size);
          });
        });

    return list;
  }

  /**
   * Creates all the menus needed for changing a widget to a different type.
   *
   * @param tile the tile for the widget to create the change menus for
   */
  private ActionList createChangeMenusForWidget(WidgetTile tile) {
    Widget widget = tile.getContent();
    ActionList list = ActionList.withName("Show as...");

    widget.getSources().stream()
        .map(s -> Components.getDefault().componentNamesForSource(s))
        .flatMap(List::stream)
        .sorted()
        .distinct()
        .forEach(name -> list.addAction(
            name,
            name.equals(widget.getName()) ? new Label("✓") : null,
            () -> {
              // no need to change it if it's already the same type
              if (!name.equals(widget.getName())) {
                Components.getDefault()
                    .createWidget(name, widget.getSources())
                    .ifPresent(newWidget -> {
                      newWidget.setTitle(widget.getTitle());
                      tile.setContent(newWidget);
                    });
              }
            }));
    return list;
  }

  /**
   * Creates the menu for editing the properties of a widget.
   *
   * @param tile the tile to pull properties from
   * @return     the edit property menu
   */
  private void showPropertySheet(Tile<?> tile) {
    ExtendedPropertySheet propertySheet = new ExtendedPropertySheet();
    propertySheet.getItems().add(new ExtendedPropertySheet.PropertyItem<>(tile.getContent().titleProperty()));
    Dialog<ButtonType> dialog = new Dialog<>();
    if (tile.getContent() instanceof Widget) {
      ((Widget) tile.getContent()).getProperties().stream()
          .map(ExtendedPropertySheet.PropertyItem::new)
          .forEachOrdered(propertySheet.getItems()::add);
    }

    dialog.setTitle("Edit widget properties");
    dialog.getDialogPane().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());
    dialog.getDialogPane().setContent(new BorderPane(propertySheet));
    dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);

    dialog.showAndWait();
  }

  private void collapseTile(Tile tile, int count, boolean left) {
    Function<TileLayout, TileLayout> moveLeft = l -> l.withCol(l.origin.col - 1);
    Function<TileLayout, TileLayout> moveUp = l -> l.withRow(l.origin.row - 1);
    Function<TileSize, TileSize> shrinkLeft = s -> new TileSize(Math.max(1, s.getWidth() - 1), s.getHeight());
    Function<TileSize, TileSize> shrinkUp = s -> new TileSize(s.getWidth(), Math.max(1, s.getHeight() - 1));
    for (int i = 0; i < count; i++) {
      Optional<Runnable> move;
      if (left) {
        move = collapseTile(tile, moveLeft, shrinkLeft, true);
      } else {
        move = collapseTile(tile, moveUp, shrinkUp, false);
      }
      if (move.isPresent()) {
        move.get().run();
      } else {
        // Can't move any further, bail
        break;
      }
    }
  }

  /**
   * Creates a {@link Runnable} that will move or shrink the given tile, as well as moving or shrinking any tiles
   * in the way of moving it. If the tile cannot be shrunk or moved, returns an empty Optional.
   *
   * @param tile                 the tile to move
   * @param targetLayoutFunction the function to use to set the origin for the target location
   * @param shrink               the function to use to shrink the tile
   */
  private Optional<Runnable> collapseTile(Tile tile,
                                          Function<TileLayout, TileLayout> targetLayoutFunction,
                                          Function<TileSize, TileSize> shrink,
                                          boolean left) {
    TileSize minSize = pane.round(tile.getContent().getView().getMinWidth(),
        tile.getContent().getView().getMinHeight());
    TileLayout layout = pane.getTileLayout(tile);
    TileLayout targetLayout = targetLayoutFunction.apply(layout);
    int importantDim = left ? layout.size.getWidth() : layout.size.getHeight();
    int minDim = left ? minSize.getWidth() : minSize.getHeight();
    if (!pane.isOverlapping(targetLayout, n -> n == tile) && !targetLayout.origin.equals(layout.origin)) { // NOPMD
      // Great, we can move it
      return Optional.of(() -> {
        GridPane.setColumnIndex(tile, targetLayout.origin.col);
        GridPane.setRowIndex(tile, targetLayout.origin.row);
      });
    } else if (importantDim > minDim) {
      // Shrink the tile
      return Optional.of(() -> tile.setSize(shrink.apply(tile.getSize())));
    } else if (!targetLayout.origin.equals(layout.origin)) { // NOPMD
      // Try to move or shrink other tiles in the way, then move this one into the free space
      int lower = left ? layout.origin.row : layout.origin.col;
      int upper = lower + importantDim;
      List<Optional<Runnable>> runs = IntStream.range(lower, upper)
          .mapToObj(i -> left ? pane.tileAt(targetLayout.origin.col, i) : pane.tileAt(i, targetLayout.origin.row))
          .flatMap(TypeUtils.optionalStream()) // guaranteed to be at least one tile
          .distinct() // need to make sure we have no repeats, or n-row tiles will get moved n times
          .filter(t -> tile != t)
          .map(t -> collapseTile(t, targetLayoutFunction, shrink, left)) // recursion here
          .collect(Collectors.toList());
      if (runs.stream().allMatch(Optional::isPresent)) {
        return Optional.of(() -> {
          runs.forEach(r -> r.get().run());
          GridPane.setColumnIndex(tile, targetLayout.origin.col);
          GridPane.setRowIndex(tile, targetLayout.origin.row);
        });
      } else {
        return Optional.empty();
      }
    } else {
      return Optional.empty();
    }
  }

  private static void destroyedSourceCouldNotBeRestored(DestroyedSource source, Throwable error) {
    log.log(Level.WARNING, "Could not restore source: " + source.getId(), error);
  }

}
