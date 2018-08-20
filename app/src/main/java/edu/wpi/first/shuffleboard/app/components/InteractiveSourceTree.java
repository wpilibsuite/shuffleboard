package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.components.FilterableTreeItem;
import edu.wpi.first.shuffleboard.api.components.SourceTreeTable;
import edu.wpi.first.shuffleboard.api.dnd.DataFormats;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.DataSourceUtils;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;

import java.util.List;
import java.util.function.Consumer;

import javafx.collections.MapChangeListener;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * An interactive source tree that allows user gestures. This encapsulates the logic used to drag sources from a tree
 * into a widget pane, and handles context menus and updating when the available sources change.
 */
public final class InteractiveSourceTree extends SourceTreeTable<SourceEntry, Object> {

  private final Consumer<Component> addComponentToActiveTab;
  private SourceEntry selectedEntry; // NOPMD could be final - false positive. PMD doesn't seem to know what a lambda is

  /**
   * Creates a new interactive source tree.
   *
   * @param sourceType              the type of the sources the tree will contain
   * @param addComponentToActiveTab a callback used to add a new component to the active tab. Used by the "Show as"
   *                                context menu
   * @param createTabForSource      a callback used to create and bring focus to a new tab to autopopulate with data
   *                                in a complex data source
   */
  public InteractiveSourceTree(SourceType sourceType,
                               Consumer<Component> addComponentToActiveTab,
                               Consumer<SourceEntry> createTabForSource) {
    super();
    this.addComponentToActiveTab = addComponentToActiveTab;
    setSourceType(sourceType);
    setRoot(new FilterableTreeItem<>(sourceType.createRootSourceEntry()));
    setShowRoot(false);
    getSelectionModel().selectedItemProperty().addListener((__, oldItem, newItem) -> {
      selectedEntry = newItem == null ? null : newItem.getValue();
    });
    setRowFactory(__ -> {
      TreeTableRow<SourceEntry> row = new TreeTableRow<>();
      makeSourceRowDraggable(row);
      return row;
    });

    setOnContextMenuRequested(e -> {
      TreeItem<SourceEntry> selectedItem = getSelectionModel().getSelectedItem();
      if (selectedItem == null) {
        return;
      }

      SourceEntry entry = selectedItem.getValue();
      DataSource<?> source = entry.get();
      List<String> componentNames = Components.getDefault().componentNamesForSource(source);

      ContextMenu menu = new ContextMenu();
      if (source.getDataType().isComplex()) {
        menu.getItems().add(FxUtils.menuItem("Create tab", __ -> createTabForSource.accept(entry)));
        if (!componentNames.isEmpty()) {
          menu.getItems().add(new SeparatorMenuItem());
        }
      } else if (componentNames.isEmpty()) {
        // Can't create a tab, and no components can display the source
        return;
      }
      componentNames.stream()
          .map(name -> createShowAsMenuItem(name, source))
          .forEach(menu.getItems()::add);

      menu.show(getScene().getWindow(), e.getScreenX(), e.getScreenY());
    });
    sourceType.getAvailableSources().addListener((MapChangeListener<String, Object>) change -> {
      SourceEntry entry = sourceType.createSourceEntryForUri(change.getKey());
      if (DataSourceUtils.isNotMetadata(entry.getName())) {
        if (change.wasAdded()) {
          updateEntry(entry);
        } else if (change.wasRemoved()) {
          removeEntry(entry);
        }
      }
    });

    // Update when the available sources chaneg
    sourceType.getAvailableSourceUris().stream()
        .filter(DataSourceUtils::isNotMetadata)
        .map(sourceType::createSourceEntryForUri)
        .forEach(this::updateEntry);
  }

  private void makeSourceRowDraggable(TreeTableRow<? extends SourceEntry> row) {
    row.setOnDragDetected(event -> {
      if (selectedEntry == null) {
        return;
      }
      Dragboard dragboard = row.startDragAndDrop(TransferMode.COPY_OR_MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(DataFormats.source, selectedEntry);
      dragboard.setContent(content);
      event.consume();
    });
  }

  private MenuItem createShowAsMenuItem(String componentName, DataSource<?> source) {
    return FxUtils.menuItem("Show as: " + componentName, __ -> {
      Components.getDefault().createComponent(componentName, source)
          .ifPresent(addComponentToActiveTab);
    });
  }

  public FilterableTreeItem<SourceEntry> getFilterableRoot() {
    return (FilterableTreeItem<SourceEntry>) super.getRoot();
  }

}
