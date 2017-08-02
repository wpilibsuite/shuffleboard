package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.NetworkTableEntry;
import edu.wpi.first.shuffleboard.data.MapData;
import edu.wpi.first.shuffleboard.data.types.MapType;
import edu.wpi.first.shuffleboard.util.EqualityUtils;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

@Description(name = "Network Table Tree", dataTypes = MapType.class)
public class NetworkTableTreeWidget extends SimpleAnnotatedWidget<MapData> {

  private final StackPane pane = new StackPane();
  private final TreeTableView<NetworkTableEntry> tree = new TreeTableView<>();

  private final TreeItem<NetworkTableEntry> root = new TreeItem<>(new NetworkTableEntry("/", null));

  /**
   * Compares tree items, branches first.
   */
  private static final Comparator<TreeItem<NetworkTableEntry>> branchesFirst
      = (o1, o2) -> Boolean.compare(o1.isLeaf(), o2.isLeaf());

  /**
   * Compares tree items alphabetically.
   */
  private static final Comparator<TreeItem<NetworkTableEntry>> alphabetical
      = Comparator.comparing(item -> item.getValue().getKey().toLowerCase(Locale.getDefault()));

  private final TreeTableColumn<NetworkTableEntry, String> keyColumn =
      new TreeTableColumn<>("Name");
  private final TreeTableColumn<NetworkTableEntry, String> valueColumn =
      new TreeTableColumn<>("Value");

  @SuppressWarnings("JavadocMethod")
  public NetworkTableTreeWidget() {
    pane.getChildren().add(tree);
    keyColumn.prefWidthProperty().bind(tree.widthProperty().divide(2).subtract(2));
    valueColumn.prefWidthProperty().bind(tree.widthProperty().divide(2).subtract(2));
    root.setExpanded(true);
    tree.setRoot(root);
    tree.setShowRoot(false);
    tree.getColumns().addAll(keyColumn, valueColumn);
    keyColumn.setCellValueFactory(
        f -> new ReadOnlyStringWrapper(getEntryForCellData(f).simpleKey()));
    valueColumn.setCellValueFactory(
        f -> new ReadOnlyStringWrapper(getEntryForCellData(f).getDisplayString()));
    tree.setSortPolicy(__ -> {
      sort(root);
      return true;
    });
    dataProperty().addListener((__, oldData, newData) -> {
      final Map<String, Object> newMap = newData.asMap();
      // Remove deleted keys
      if (oldData != null) {
        oldData.asMap().entrySet().stream()
            .filter(e -> !newMap.containsKey(e.getKey()))
            .forEach(e -> makeBranches(e.getKey(), e.getValue(), true));
      }

      newData.changesFrom(oldData).forEach((key, value) -> makeBranches(key, value, false));
    });
  }

  @Override
  public Pane getView() {
    return pane;
  }

  private NetworkTableEntry getEntryForCellData(
      TreeTableColumn.CellDataFeatures<NetworkTableEntry, String> features) {
    return features.getValue().getValue();
  }

  /**
   * Sorts tree nodes recursively in order of branches before leaves, then alphabetically.
   *
   * @param node the root node to sort
   */
  private void sort(TreeItem<NetworkTableEntry> node) {
    if (!node.isLeaf()) {
      FXCollections.sort(node.getChildren(),
          branchesFirst.thenComparing(alphabetical));
      node.getChildren().forEach(this::sort);
    }
  }

  /**
   * Creates, updates, or deletes tree nodes in the network table view.
   *
   * @param fullKey the network table key that changed
   * @param value   the value of the entry that changed
   * @param deleted {@code true} if the entry was deleted, {@code false} otherwise
   */
  private void makeBranches(String fullKey, Object value, boolean deleted) {
    String key = NetworkTableUtils.normalizeKey(fullKey);
    List<String> pathElements = Stream.of(key.split("/"))
        .filter(s -> !s.isEmpty())
        .collect(Collectors.toList());
    TreeItem<NetworkTableEntry> current = root;
    TreeItem<NetworkTableEntry> parent = root;
    StringBuilder currentKey = new StringBuilder();

    // Get the appropriate node for the value, creating branches as needed
    for (int i = 0; i < pathElements.size(); i++) {
      String pathElement = pathElements.get(i);
      currentKey.append('/').append(pathElement);
      parent = current;
      current = current.getChildren().stream()
          .filter(item -> item.getValue().getKey().equals(currentKey.toString()))
          .findFirst()
          .orElse(null);
      if (!deleted && i < pathElements.size() - 1 && current == null) {
        // It's a branch (subtable); expand it
        current = new TreeItem<>(new NetworkTableEntry(currentKey.toString(), ""));
        current.setExpanded(true);
        parent.getChildren().add(current);
      }
    }

    // Remove, update, or create the value node as needed
    if (deleted) {
      if (current != null) {
        parent.getChildren().remove(current);
      }
    } else {
      if (current == null) {
        // Newly added value, create a tree item for it
        current = new TreeItem<>(new NetworkTableEntry(key, value));
        parent.getChildren().add(current);
      } else if (EqualityUtils.isDifferent(current.getValue(), value)) {
        // The value updated, so just update the existing node
        current.setValue(new NetworkTableEntry(key, value));
      }
    }
    tree.sort();
  }

  public TreeTableView<NetworkTableEntry> getTree() {
    return tree;
  }

  public TreeTableColumn<NetworkTableEntry, String> getKeyColumn() {
    return keyColumn;
  }

  public TreeTableColumn<NetworkTableEntry, String> getValueColumn() {
    return valueColumn;
  }

}
