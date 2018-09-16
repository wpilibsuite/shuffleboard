package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.components.FilterableTreeItem;
import edu.wpi.first.shuffleboard.api.components.SourceTreeTable;
import edu.wpi.first.shuffleboard.api.data.MapData;
import edu.wpi.first.shuffleboard.api.sources.DataSourceUtils;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceEntry;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;

import edu.wpi.first.networktables.NetworkTable;

import java.util.Map;

import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

@Description(name = "Network Table Tree", dataTypes = MapData.class)
public class NetworkTableTreeWidget extends SimpleAnnotatedWidget<MapData> {

  private final StackPane pane = new StackPane();
  private final SourceTreeTable<NetworkTableSourceEntry, String> tree = new SourceTreeTable<>();

  @SuppressWarnings("JavadocMethod")
  public NetworkTableTreeWidget() {
    tree.setSourceType(NetworkTableSourceType.getInstance());
    pane.getChildren().add(tree);
    NetworkTableSourceEntry rootEntry = new NetworkTableSourceEntry("/", null);
    TreeItem<NetworkTableSourceEntry> root = new FilterableTreeItem<>(rootEntry);
    root.setExpanded(true);
    tree.setRoot(root);
    tree.setShowRoot(false);
    dataOrDefault.addListener((__, oldData, newData) -> {
      final Map<String, Object> newMap = newData.asMap();
      // Remove deleted keys
      if (oldData != null) {
        oldData.asMap().entrySet().stream()
            .filter(e -> !newMap.containsKey(e.getKey()))
            .forEach(e -> tree.removeEntry(new NetworkTableSourceEntry(e.getKey(), e.getValue())));
      }

      newData.changesFrom(oldData)
          .forEach((key, value) -> {
            if (DataSourceUtils.isNotMetadata(key)) {
              tree.updateEntry(new NetworkTableSourceEntry(NetworkTable.normalizeKey(key), value));
            }
          });
    });
  }

  @Override
  public Pane getView() {
    return pane;
  }

  public SourceTreeTable<NetworkTableSourceEntry, String> getTree() {
    return tree;
  }

}
