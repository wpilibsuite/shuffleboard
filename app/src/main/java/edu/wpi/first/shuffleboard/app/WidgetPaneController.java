package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.dnd.DataFormats;
import edu.wpi.first.shuffleboard.api.prefs.Category;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.util.RoundingMode;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.ComponentContainer;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.LayoutType;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.app.components.LayoutTile;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.TileLayout;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;
import edu.wpi.first.shuffleboard.app.components.WidgetTile;
import edu.wpi.first.shuffleboard.app.dnd.TileDragResizer;
import edu.wpi.first.shuffleboard.app.json.SourcedRestorer;
import edu.wpi.first.shuffleboard.app.prefs.AppPreferences;
import edu.wpi.first.shuffleboard.app.prefs.SettingsDialog;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import org.fxmisc.easybind.EasyBind;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.binding.Binding;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import static java.util.stream.Collectors.toSet;

// needs refactoring to split out per-widget interaction
@SuppressWarnings("PMD.GodClass")
public class WidgetPaneController {

  private static final Logger log = Logger.getLogger(WidgetPaneController.class.getName());

  @FXML
  private WidgetPane pane;

  private final Set<Node> tilesAlreadySetup = Collections.newSetFromMap(new WeakHashMap<>());

  private TileSelector selector;
  private TileMover tileMover;

  @FXML
  private void initialize() {
    selector = TileSelector.forPane(pane);
    tileMover = new TileMover(pane);

    pane.getTiles().addListener((ListChangeListener<Tile>) changes -> {
      while (changes.next()) {
        changes.getAddedSubList().forEach(this::setupTile);
      }
    });

    // Add a context menu for pane-related actions
    pane.setOnContextMenuRequested(this::createPaneContextMenu);

    WidgetPaneDragHandler paneDragHandler = new WidgetPaneDragHandler(pane);
    pane.setOnDragOver(paneDragHandler);
    pane.setOnDragDone(paneDragHandler);
    pane.setOnDragExited(paneDragHandler);
    pane.setOnDragDropped(paneDragHandler);

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
            .forEach(tile -> tileMover.collapseTile(tile, oldCount - newCount, TileMover.Direction.HORIZONTAL));
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
            .forEach(tile -> tileMover.collapseTile(tile, oldCount - newCount, TileMover.Direction.VERTICAL));
      }
    });

    // Handle restoring data sources after a widget is added from a save file before its source(s) are available
    SourceTypes.getDefault().allAvailableSourceUris().addListener((ListChangeListener<String>) c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          // Restore sources for top-level Sourced objects
          SourcedRestorer restorer = new SourcedRestorer();
          List<? extends String> addedUris = c.getAddedSubList();
          restoreSources(restorer, addedUris);
        } else if (c.wasRemoved()) {
          List<? extends String> removedUris = c.getRemoved();
          // Replace all removed sources with DestroyedSources
          removeSources(removedUris);
        }
      }
    });
  }

  private void restoreSources(SourcedRestorer restorer, List<? extends String> addedUris) {
    pane.getTiles().stream()
        .map(Tile::getContent)
        .flatMap(TypeUtils.castStream(Sourced.class))
        .forEach(sourced -> restorer.restoreSourcesFor(
            sourced,
            addedUris,
            WidgetPaneController::destroyedSourceCouldNotBeRestored));

    // Restore sources for all nested Sourced objects
    pane.getTiles().stream()
        .map(Tile::getContent)
        .flatMap(TypeUtils.castStream(ComponentContainer.class))
        .flatMap(ComponentContainer::allComponents)
        .flatMap(TypeUtils.castStream(Sourced.class))
        .forEach(sourced -> restorer.restoreSourcesFor(
            sourced,
            addedUris,
            WidgetPaneController::destroyedSourceCouldNotBeRestored));
  }

  private void removeSources(List<? extends String> removedUris) {
    pane.getTiles().stream()
        .map(Tile::getContent)
        .flatMap(Component::allComponents)
        .flatMap(TypeUtils.castStream(Sourced.class))
        .forEach(s -> replaceWithDestroyedSource(s, removedUris));
  }

  private void createPaneContextMenu(ContextMenuEvent e) {
    // Menu for adding new empty layouts
    // As an alternative to adding a component, then having to add that to a new layout
    // This reduces the complexity of creating new layouts
    Menu addLayouts = new Menu("Add layout...");
    Components.getDefault().allComponents()
        .flatMap(TypeUtils.castStream(LayoutType.class))
        .map(ComponentType::getName)
        .sorted()
        .map(c -> FxUtils.menuItem(c, __ -> pane.addComponent(Components.getDefault().createComponent(c).get())))
        .forEach(addLayouts.getItems()::add);

    // Removes all the tiles from the pane. Destructive operation!
    MenuItem clear = FxUtils.menuItem("Clear", __ -> {
      List<Tile> tiles = new ArrayList<>(pane.getTiles());
      tiles.stream()
          .map((Function<Tile, Component>) pane::removeTile)
          .flatMap(Component::allComponents)
          .flatMap(TypeUtils.castStream(Sourced.class))
          .forEach(Sourced::removeAllSources);
    });
    ContextMenu contextMenu = new ContextMenu(addLayouts, new SeparatorMenuItem(), clear);
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

  private void dragMultipleTiles(Set<Tile<?>> tiles, GridPoint initialPoint) {
    Dragboard dragboard = pane.startDragAndDrop(TransferMode.MOVE);
    ClipboardContent content = new ClipboardContent();
    Set<String> tileIds = tiles.stream()
        .map(Tile::getId)
        .collect(toSet());
    content.put(DataFormats.multipleTiles, new DataFormats.MultipleTileData(tileIds, initialPoint));
    dragboard.setContent(content);
  }

  /**
   * Starts the drag of the given widget tile.
   */
  private void dragSingleTile(Tile<?> tile, GridPoint point) {
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
    content.put(DataFormats.singleTile, new DataFormats.TileData(tile.getId(), point));
    dragboard.setContent(content);
  }

  private void startTileDrag(Tile<?> tile, MouseEvent event) {
    if (TileDragResizer.makeResizable(pane, tile).isDragging()) {
      // don't drag the widget while it's being resized
      selector.deselectAll();
      return;
    }
    if (selector.isSelected(tile) && selector.getSelectedTiles().size() > 1) {
      // Drag point is a point in the pane
      Point2D localPoint = pane.screenToLocal(event.getScreenX(), event.getScreenY());
      GridPoint dragPoint = pane.pointAt(localPoint.getX(), localPoint.getY());
      dragMultipleTiles(selector.getSelectedTiles(), dragPoint);
    } else {
      selector.deselectAll();
      // Drag point is a point on the tile
      GridPoint dragPoint = new GridPoint(pane.roundWidthToNearestTile(event.getX()) - 1,
          pane.roundHeightToNearestTile(event.getY()) - 1);
      dragSingleTile(tile, dragPoint);
    }
    event.consume();
  }

  private void mousePressOnTile(Tile<?> tile, MouseEvent e) {
    if (e.isControlDown() && e.isPrimaryButtonDown()) {
      if (selector.areTilesSelected()) {
        selector.toggleSelect(tile);
      } else {
        selector.select(tile);
      }
    } else if (!selector.isSelected(tile)) {
      selector.deselectAll();
    }
  }

  /**
   * Sets up a tile with a context menu and lets it be dragged around.
   */
  private void setupTile(Tile tile) {
    if (tilesAlreadySetup.contains(tile)) {
      return;
    }
    tilesAlreadySetup.add(tile);
    TileDragResizer.makeResizable(pane, tile);

    tile.setOnContextMenuRequested(event -> {
      ContextMenu contextMenu = createContextMenu(event);
      contextMenu.show(pane.getScene().getWindow(), event.getScreenX(), event.getScreenY());
      event.consume();
    });

    // Allow users to double-click on a tile to select it.
    tile.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
      if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
        selector.selectOnly(tile);
      }
    });

    ActionList.registerSupplier(tile, () -> {
      ActionList widgetPaneActions = ActionList
          .withName(tile.getContent().getTitle())
          .addAction("Remove", () -> {
            if (selector.isSelected(tile)) {
              selector.getSelectedTiles().forEach(this::removeTile);
            } else {
              removeTile(tile);
            }
            selector.deselectAll();
          })
          .addNested(createLayoutMenus(tile));

      if (tile instanceof WidgetTile) {
        WidgetTile widgetTile = (WidgetTile) tile;
        ActionList changeMenus = createChangeMenusForWidget(widgetTile);
        if (changeMenus.hasItems()) {
          widgetPaneActions.addNested(changeMenus);
        }
      }
      widgetPaneActions.addAction("Edit Properties",
          () -> {
            Set<Tile<?>> tiles = new LinkedHashSet<>();
            tiles.add(tile);
            tiles.addAll(selector.getSelectedTiles());
            if (tiles.size() == 1) {
              showSettingsDialog(createSettingsCategoriesForComponent(tile.getContent()));
            } else {
              List<Category> categories = tiles.stream()
                  .map(t -> t.getContent())
                  .map(c -> createSettingsCategoriesForComponent(c))
                  .collect(Collectors.toList());
              showSettingsDialog(categories);
            }
          });

      // Layout unwrapping
      if (tile instanceof LayoutTile) {
        widgetPaneActions.addAction("Unwrap", () -> {
          if (selector.areTilesSelected()
              && selector.getSelectedTiles().stream().allMatch(t -> t instanceof LayoutTile)) {
            selector.getSelectedTiles().forEach(t -> unwrapLayout((LayoutTile) t));
          } else {
            unwrapLayout(tile);
          }
          selector.deselectAll();
        });
      }
      return widgetPaneActions;
    });

    tile.setOnDragDetected(event -> startTileDrag(tile, event));

    tile.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> mousePressOnTile(tile, e));

    tile.setOnDragDropped(new TileDropHandler(pane, tile));
  }

  private void removeTile(Tile<?> tile) {
    if (pane.getTiles().contains(tile)) {
      Component removed = pane.removeTile(tile);
      removed.allComponents()
          .flatMap(TypeUtils.castStream(Sourced.class))
          .forEach(Sourced::removeAllSources);
    }
  }

  private void unwrapLayout(Tile<? extends Layout> tile) {
    Layout layout = tile.getContent();
    layout.components()
        .collect(Collectors.toList()) // Collect into temporary list to prevent ConcurrentModificationExceptions
        .stream()
        .filter(pane::canAdd)
        .forEach(component -> {
          layout.removeComponent(component);
          pane.addComponent(component);
        });
    if (layout.components().count() == 0) {
      // No point in keeping the empty layout around, remove it
      pane.removeTile(tile);
    }
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
        .map(ComponentType::getName)
        .forEach(name -> {
          list.addAction(name, () -> {
            TileLayout was = pane.getTileLayout(tile);
            Component content = pane.removeTile(tile);
            Layout layout = (Layout) Components.getDefault().createComponent(name).get();
            layout.addChild(content);
            if (selector.getSelectedTiles().size() > 1) {
              for (Tile<?> t : selector.getSelectedTiles()) {
                if (tile.equals(t)) {
                  continue;
                }
                layout.addChild(pane.removeTile(t));
              }
            }
            pane.addComponent(layout, was.origin, was.size);
            selector.deselectAll();
          });
        });

    return list;
  }

  /**
   * Returns the set of items that are shared between the source collection and all the tests.
   *
   * @param source the source collection containing the items to check
   * @param tests  the collections to check
   * @param <T>    the type of the items to check.
   *
   * @return the set of items that are shared between all the provided collections
   */
  private static <T> Set<T> allContain(Collection<T> source, Collection<? extends Collection<T>> tests) {
    Set<T> items = new HashSet<>();
    for (T item : source) {
      if (tests.stream().allMatch(c -> c.contains(item))) {
        items.add(item);
      }
    }
    return items;
  }

  private static Set<DataType> getDataTypes(Sourced sourced) {
    return sourced.getSources().stream()
        .map(DataSource::getDataType)
        .collect(toSet());
  }

  private void changeAllWidgetTiles(String newWidgetName) {
    selector.getSelectedTiles()
        .stream()
        .map(t -> (WidgetTile) t)
        .forEach(t -> {
          Components.getDefault()
              .createWidget(newWidgetName, t.getContent().getSources())
              .ifPresent(newWidget -> {
                newWidget.setTitle(t.getContent().getTitle());
                t.setContent(newWidget);
              });
        });
    selector.deselectAll();
  }

  /**
   * Creates all the menus needed for changing a widget to a different type.
   *
   * @param tile the tile for the widget to create the change menus for
   */
  private ActionList createChangeMenusForWidget(Tile<Widget> tile) {
    Widget widget = tile.getContent();
    ActionList list = ActionList.withName("Show as...");

    if (selector.areTilesSelected() && selector.isSelected(tile)) {
      boolean allWidgets = selector.getSelectedTiles().stream().allMatch(t -> t.getContent() instanceof Widget);
      if (allWidgets) {
        List<Set<DataType>> collect = selector.getSelectedTiles().stream()
            .map(Tile::getContent)
            .flatMap(TypeUtils.castStream(Widget.class))
            .map(w -> getDataTypes(w))
            .collect(Collectors.toList());
        Set<DataType> commonDataTypes = allContain(tile.getContent().getDataTypes(), collect);
        if (commonDataTypes.isEmpty()) {
          return ActionList.withName(null);
        }
        commonDataTypes.stream()
            .map(Components.getDefault()::componentNamesForType)
            .flatMap(List::stream)
            .distinct()
            .sorted()
            .forEach(name -> list.addAction(
                name,
                name.equals(widget.getName()) ? new Label("✓") : null,
                () -> changeAllWidgetTiles(name)
            ));
        return list;
      } else {
        return ActionList.withName(null);
      }
    } else {
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
    }
    return list;
  }

  /**
   * Creates and displays a dialog for editing settings.
   *
   * @param categories the root categories to display in the settings dialog
   */
  private void showSettingsDialog(List<Category> categories) {
    SettingsDialog dialog = new SettingsDialog(categories);

    dialog.setTitle("Edit Properties");
    dialog.getDialogPane().getStylesheets().setAll(AppPreferences.getInstance().getTheme().getStyleSheets());

    dialog.showAndWait();
  }

  /**
   * Creates and displays a dialog for editing settings.
   *
   * @param categories the root categories to display in the settings dialog
   */
  private void showSettingsDialog(Category... categories) {
    showSettingsDialog(Arrays.asList(categories));
  }

  /**
   * Creates a category for a component, setting subcategories as necessary if it contains other components.
   *
   * @param component the component to create a settings category for
   *
   * @return a new settings category
   */
  private Category createSettingsCategoriesForComponent(Component component) {
    List<Group> groups = new ArrayList<>(component.getSettings());
    groups.add(titleGroup(component));
    String categoryName = component.getTitle().isEmpty() ? "Unnamed " + component.getName() : component.getTitle();
    if (component instanceof ComponentContainer) {
      List<Category> subCategories = ((ComponentContainer) component).components()
          .map(this::createSettingsCategoriesForComponent)
          .collect(Collectors.toList());
      return Category.of(categoryName, subCategories, groups);
    } else {
      return Category.of(categoryName, groups);
    }
  }

  /**
   * Creates a settings group for the title property of a component.
   *
   * @param component the component to create the settings group for
   *
   * @return a new settings group
   */
  private Group titleGroup(Component component) {
    return Group.of("Miscellaneous",
        Setting.of(
            "Title",
            "The title of this " + component.getName().toLowerCase(Locale.US),
            component.titleProperty()
        )
    );
  }

  private static void destroyedSourceCouldNotBeRestored(DestroyedSource source, Throwable error) {
    log.log(Level.WARNING, "Could not restore source: " + source.getId(), error);
  }

}
