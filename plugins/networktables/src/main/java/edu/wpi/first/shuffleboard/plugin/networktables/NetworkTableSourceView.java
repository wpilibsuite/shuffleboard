package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.api.dnd.DataFormats;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.widget.Widgets;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableEntry;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSource;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;

import java.util.List;
import java.util.stream.Collectors;

import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;

public class NetworkTableSourceView extends StackPane {

  private final NetworkTableTreeWidget widget = new NetworkTableTreeWidget();
  private NetworkTableEntry selectedEntry;
  private DashboardTabPane dashboard;

  public NetworkTableSourceView() {
    widget.setSource(NetworkTableSource.forKey("/"));
    widget.getTree().getSelectionModel().selectedItemProperty().addListener((__, prev, cur) -> {
      if (cur == null) {
        selectedEntry = null;
      } else {
        selectedEntry = cur.getValue();
      }
    });
    widget.getTree().setRowFactory(view -> {
      TreeTableRow<NetworkTableEntry> row = new TreeTableRow<>();
      row.hoverProperty().addListener((__, wasHover, isHover) -> {
        if (!row.isEmpty()) {
          List<String> toHighlight = NetworkTableUtils.getHierarchy(row.getTreeItem().getValue().getKey())
              .stream()
              .map(NetworkTableSourceType.INSTANCE::toUri)
              .collect(Collectors.toList());
        }
      });
      makeSourceRowDraggable(row);
      return row;
    });

    widget.getTree().setOnContextMenuRequested(e -> {
      TreeItem<NetworkTableEntry> selectedItem = widget.getTree().getSelectionModel().getSelectedItem();
      if (selectedItem == null) {
        return;
      }

      DataSource<?> source = selectedItem.getValue().get();
      List<String> widgetNames = Widgets.widgetNamesForSource(source);
      if (widgetNames.isEmpty()) {
        // No known widgets that can show this data
        return;
      }

      ContextMenu menu = new ContextMenu();
      widgetNames.stream()
          .map(name -> createShowAsMenuItem(name, source))
          .forEach(menu.getItems()::add);

      menu.show(widget.getTree().getScene().getWindow(), e.getScreenX(), e.getScreenY());
    });

    getChildren().setAll(widget.getView());
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

  private MenuItem createShowAsMenuItem(String widgetName, DataSource<?> source) {
    MenuItem menuItem = new MenuItem("Show as: " + widgetName);
    menuItem.setOnAction(action -> {
      Widgets.createWidget(widgetName, source)
          .ifPresent(dashboard::addWidgetToActivePane);
    });
    return menuItem;
  }

  public void setDashboard(DashboardTabPane dashboard) {
    this.dashboard = dashboard;
  }

}
