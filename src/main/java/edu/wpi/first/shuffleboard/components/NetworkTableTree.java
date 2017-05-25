package edu.wpi.first.shuffleboard.components;

import edu.wpi.first.shuffleboard.NetworkTableEntry;
import edu.wpi.first.shuffleboard.util.FxUtils;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.tables.ITable;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * A special version of a tree table view that displays network table entries that update in real
 * time.
 */
public class NetworkTableTree extends TreeTableView<NetworkTableEntry> {

  private final TreeItem<NetworkTableEntry> root = new TreeItem<>(new NetworkTableEntry("/", null));

  /**
   * Compares tree items, branches first.
   */
  private final Comparator<TreeItem<NetworkTableEntry>> branchesFirst
      = (o1, o2) -> o1.isLeaf() ? o2.isLeaf() ? 0 : 1 : -1;

  /**
   * Compares tree items alphabetically.
   */
  private final Comparator<TreeItem<NetworkTableEntry>> alphabetical
      = Comparator.comparing(item -> item.getValue().getKey());

  private final TreeTableColumn<NetworkTableEntry, String> keyColumn =
      new TreeTableColumn<>("Name");
  private final TreeTableColumn<NetworkTableEntry, String> valueColumn =
      new TreeTableColumn<>("Value");

  /**
   * Creates a new network table tree view.
   */
  public NetworkTableTree() {
    super();
    getColumns().addAll(keyColumn, valueColumn);
    keyColumn.setCellValueFactory(
        f -> new ReadOnlyStringWrapper(getEntryForCellData(f).simpleKey()));
    valueColumn.setCellValueFactory(
        f -> new ReadOnlyStringWrapper(getEntryForCellData(f).getDisplayString()));
    setRoot(root);
    setSortPolicy(param -> {
      sort(getRoot());
      return true;
    });
    NetworkTablesJNI.addEntryListener(
        "",
        (uid, key, value, flags) -> FxUtils.runOnFxThread(() -> makeBranches(key, value, flags)),
        0xFF);
  }

  public TreeTableColumn<NetworkTableEntry, String> getKeyColumn() {
    return keyColumn;
  }

  public TreeTableColumn<NetworkTableEntry, String> getValueColumn() {
    return valueColumn;
  }

  private NetworkTableEntry getEntryForCellData(
      CellDataFeatures<NetworkTableEntry, String> features) {
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
   * @param flags   the flags of the change
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  private void makeBranches(String fullKey, Object value, int flags) {
    String key = NetworkTableUtils.normalizeKey(fullKey);
    boolean deleted = (flags & ITable.NOTIFY_DELETE) != 0;
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
      } else {
        // The value updated, so just update the existing node
        current.setValue(new NetworkTableEntry(key, value));
      }
    }
    sort();
  }

}
