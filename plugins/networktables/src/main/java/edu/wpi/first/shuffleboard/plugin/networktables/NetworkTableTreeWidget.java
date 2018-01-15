package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.shuffleboard.api.components.SourceTreeTable;
import edu.wpi.first.shuffleboard.api.data.MapData;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceEntry;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;

import java.util.Map;

import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import static edu.wpi.first.shuffleboard.api.components.SourceTreeTable.alphabetical;
import static edu.wpi.first.shuffleboard.api.components.SourceTreeTable.branchesFirst;

@Description(name = "Network Table Tree", dataTypes = MapData.class)
public class NetworkTableTreeWidget extends SimpleAnnotatedWidget<MapData> {

  private final StackPane pane = new StackPane();
  private final SourceTreeTable<NetworkTableSourceEntry, String> tree = new SourceTreeTable<>();

  private final TreeItem<NetworkTableSourceEntry> root = new TreeItem<>(new NetworkTableSourceEntry("/", null));

  @SuppressWarnings("JavadocMethod")
  public NetworkTableTreeWidget() {
    tree.setSourceType(NetworkTableSourceType.getInstance());
    pane.getChildren().add(tree);
    root.setExpanded(true);
    tree.setRoot(root);
    tree.setShowRoot(false);
    tree.setSortPolicy(__ -> {
      sort(root);
      return true;
    });
    dataOrDefault.addListener((__, oldData, newData) -> {
      final Map<String, Object> newMap = newData.asMap();
      // Remove deleted keys
      if (oldData != null) {
        oldData.asMap().entrySet().stream()
            .filter(e -> !newMap.containsKey(e.getKey()))
            .forEach(e -> tree.removeEntry(new NetworkTableSourceEntry(e.getKey(), e.getValue())));
      }

      newData.changesFrom(oldData)
          .forEach((key, value) ->
              tree.updateEntry(new NetworkTableSourceEntry(
                  NetworkTable.normalizeKey(key), value)));
    });
  }

  @Override
  public Pane getView() {
    return pane;
  }

  /**
   * Sorts tree nodes recursively in order of branches before leaves, then alphabetically.
   *
   * @param node the root node to sort
   */
  private void sort(TreeItem<? extends SourceEntry> node) {
    if (!node.isLeaf()) {
      FXCollections.sort(node.getChildren(),
          branchesFirst.thenComparing(alphabetical));
      node.getChildren().forEach(this::sort);
    }
  }

  public SourceTreeTable<NetworkTableSourceEntry, String> getTree() {
    return tree;
  }

}
