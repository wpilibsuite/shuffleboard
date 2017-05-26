package edu.wpi.first.shuffleboard.components;

import com.google.common.collect.ImmutableList;
import edu.wpi.first.shuffleboard.WidgetHandle;
import edu.wpi.first.shuffleboard.dnd.DataFormats;
import edu.wpi.first.shuffleboard.dnd.DataSourceTransferable;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.util.GridPoint;
import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.shuffleboard.widget.TileSize;
import edu.wpi.first.shuffleboard.widget.Widget;
import edu.wpi.first.shuffleboard.widget.Widgets;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * A type of tile pane specifically for widgets.
 */
public class WidgetPane extends TilePane {

  private final List<WidgetHandle> widgetHandles = new ArrayList<>();
  private final Pane gridHighlight = new StackPane();

  /**
   * Creates a new widget pane. This sets up everything needed for dragging widgets and sources
   * around in this pane.
   */
  public WidgetPane() {

    gridHighlight.getStyleClass().add("grid-highlight");

    // Handle being dragged over
    setOnDragOver(event -> {
      event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
      setGridLinesVisible(true);
      getChildren().remove(gridHighlight);
      GridPoint point = pointAt(event.getX(), event.getY());
      boolean isWidget = event.getDragboard().hasContent(DataFormats.widget);

      // preview the location of the widget if one is being dragged
      if (isWidget) {
        String widgetId = (String) event.getDragboard().getContent(DataFormats.widget);
        handleMatching(handle -> handle.getId().equals(widgetId))
            .ifPresent(handle -> previewWidget(handle, point));
      }

      // setting grid lines visible puts them above every child, so move every widget view
      // to the front to avoid them being obscure by the grid lines
      // this is a limitation of the JavaFX API
      widgetHandles.stream()
                   .map(WidgetHandle::getUiElement)
                   .forEach(Node::toFront);
      event.consume();
    });

    // Clean up widget drags when the drag exits this pane
    setOnDragDone(__ -> cleanupWidgetDrag());
    setOnDragExited(__ -> cleanupWidgetDrag());

    // Handle dropping stuff onto this pane
    setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      GridPoint point = pointAt(event.getX(), event.getY());
      if (dragboard.hasContent(DataFormats.source)) {
        DataSourceTransferable sourceTransferable =
            (DataSourceTransferable) dragboard.getContent(DataFormats.source);

        DataSource<?> source = sourceTransferable.createSource();
        dropSource(source, point);
      } else if (dragboard.hasContent(DataFormats.widget)) {
        String widgetId = (String) dragboard.getContent(DataFormats.widget);
        handleMatching(handle -> handle.getId().equals(widgetId))
            .ifPresent(handle -> dropWidget(handle, point));
      }
      cleanupWidgetDrag();
      event.consume();
    });
  }

  /**
   * Gets a read-only list of all the widget handles for this pane.
   */
  public ImmutableList<WidgetHandle> getWidgetHandles() {
    return ImmutableList.copyOf(widgetHandles);
  }

  /**
   * Gets the first widget handle that matches the given predicate.
   *
   * @param predicate the predicate to use to find the desired widget handle
   */
  public Optional<WidgetHandle> handleMatching(Predicate<WidgetHandle> predicate) {
    return widgetHandles.stream()
                        .filter(predicate)
                        .findFirst();
  }

  /**
   * Previews the widget for the given handle.
   *
   * @param handle the handle for the widget to preview
   * @param point  the point to preview the widget at
   */
  private void previewWidget(WidgetHandle handle, GridPoint point) {
    TileSize size = handle.getCurrentSize();
    add(gridHighlight, point, size);
    gridHighlight.pseudoClassStateChanged(
        PseudoClass.getPseudoClass("colliding"),
        !isOpen(point, size, handle.getUiElement(), gridHighlight));
  }

  /**
   * Drops a widget into this pane at the given point.
   *
   * @param handle the handle for the widget to drop
   * @param point  the point in the tile pane to drop the widget at
   */
  private void dropWidget(WidgetHandle handle, GridPoint point) {
    TileSize size = handle.getCurrentSize();
    if (isOpen(point, size, gridHighlight, handle.getUiElement())) {
      setLocation(handle.getUiElement(), point);
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
    Widgets.widgetNamesForSource(source)
           .stream()
           .findAny()
           .flatMap(name -> Widgets.createWidget(name, source))
           .ifPresent(this::addWidget);
    widgetHandles.stream()
                 .filter(handle -> handle.getWidget().getSource() == source) // intentional ==
                 .map(WidgetHandle::getUiElement)
                 .findAny()
                 .ifPresent(node -> setLocation(node, point));
  }

  /**
   * Cleans up from dragging widgets around in the tile pane.
   */
  private void cleanupWidgetDrag() {
    setGridLinesVisible(false);
    getChildren().remove(gridHighlight);
  }

  /**
   * Adds a widget to the tile view in the first available location.
   *
   * @param widget the widget to add
   */
  public WidgetHandle addWidget(Widget<?> widget) {
    Pane view = widget.getView();
    double width = Math.max(getTileSize(), view.getPrefWidth());
    double height = Math.max(getTileSize(), view.getPrefHeight());

    TileSize size = new TileSize((int) (width / getTileSize()),
                                 (int) (height / getTileSize()));
    return addWidget(widget, size);
  }

  /**
   * Adds a widget to the tile view in the first available location. The tile will be the specified
   * size.
   *
   * @param widget the widget to add
   * @param size   the size of the tile used to display the widget
   */
  public WidgetHandle addWidget(Widget<?> widget, TileSize size) {
    WidgetHandle handle = new WidgetHandle(widget);
    handle.setCurrentSize(size);
    widgetHandles.add(handle);
    Pane control = widget.getView();
    Node uiElement = addTile(control, size);
    uiElement.setOnDragDetected(event -> {
      dragWidget(handle);
      event.consume();
    });
    handle.setUiElement(uiElement);
    uiElement.setOnContextMenuRequested(event -> {
      ContextMenu menu = createContextMenu(handle);
      menu.show(getScene().getWindow(), event.getScreenX(), event.getScreenY());
    });
    return handle;
  }

  /**
   * Starts the drag of the given widget handle.
   */
  private void dragWidget(WidgetHandle handle) {
    Node uiElement = handle.getUiElement();
    Dragboard dragboard = uiElement.startDragAndDrop(TransferMode.MOVE);
    WritableImage preview =
        new WritableImage(
            (int) uiElement.getBoundsInParent().getWidth(),
            (int) uiElement.getBoundsInParent().getHeight()
        );
    uiElement.snapshot(null, preview);
    dragboard.setDragView(preview);
    ClipboardContent content = new ClipboardContent();
    content.put(DataFormats.widget, handle.getId());
    dragboard.setContent(content);
  }

  public void removeWidget(WidgetHandle handle) {
    widgetHandles.remove(handle);
    getChildren().remove(handle.getUiElement());
  }


  /**
   * Creates the context menu for a widget.
   *
   * @param handle the handle for the widget to create a context menu for
   */
  private ContextMenu createContextMenu(WidgetHandle handle) {
    ContextMenu menu = new ContextMenu();
    MenuItem remove = new MenuItem("Remove");
    remove.setOnAction(__ -> removeWidget(handle));
    menu.getItems().addAll(createChangeMenus(handle), new SeparatorMenuItem(), remove);
    return menu;
  }

  /**
   * Creates all the menus needed for changing a widget to a different type.
   *
   * @param handle the handle for the widget to create the change menus for
   */
  private MenuItem createChangeMenus(WidgetHandle handle) {
    Widget<?> widget = handle.getWidget();
    Menu changeView = new Menu("Show as...");
    Widgets.widgetNamesForType(DataType.valueOf(widget.getSource().getData().getClass()))
           .stream()
           .sorted()
           .forEach(name -> {
             MenuItem changeItem = new MenuItem(name);
             if (name.equals(widget.getName())) {
               changeItem.setGraphic(new Label("✓"));
             } else {
               // only need to change if it's to another type
               changeItem.setOnAction(__ -> {
                 // TODO this has a lot of room for improvement
                 removeWidget(handle);
                 Widgets.createWidget(name, widget.getSource())
                        .ifPresent(this::addWidget);
               });
             }
             changeView.getItems().add(changeItem);
           });
    return changeView;
  }

}
