package edu.wpi.first.shuffleboard.elements;

import edu.wpi.first.shuffleboard.NetworkTableEntry;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.tables.ITable;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static edu.wpi.first.shuffleboard.util.NetworkTableUtils.normalizeKey;
import static edu.wpi.first.shuffleboard.util.NetworkTableUtils.simpleKey;

/**
 * A special version of a tree table view that displays network table entries that update in real
 * time.
 */
public class NetworkTableTree extends TreeTableView<NetworkTableEntry> {

  private final TreeItem<NetworkTableEntry> root = new TreeItem<>(new NetworkTableEntry("/", null));

  private final Comparator<TreeItem<NetworkTableEntry>> leafComparator
      = (a, b) -> a.isLeaf() ? b.isLeaf() ? 0 : 1 : -1;
  private final Comparator<TreeItem<NetworkTableEntry>> nodeComparator
      = leafComparator.thenComparing(Comparator.comparing(item -> item.getValue().getKey()));

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
    keyColumn.setCellValueFactory(f -> new ReadOnlyStringWrapper(simpleKey(getEntry(f).getKey())));
    valueColumn.setCellValueFactory(f -> new ReadOnlyStringWrapper(getEntry(f).getValue()));
    setRoot(root);
    setSortPolicy(param -> {
      sort(getRoot());
      return true;
    });
    NetworkTablesJNI.addEntryListener(
        "",
        (uid, key, value, flags) -> makeBranches(key, value, flags),
        0xFF);
  }

  public TreeTableColumn<NetworkTableEntry, String> getKeyColumn() {
    return keyColumn;
  }

  public TreeTableColumn<NetworkTableEntry, String> getValueColumn() {
    return valueColumn;
  }

  private NetworkTableEntry getEntry(CellDataFeatures<NetworkTableEntry, String> features) {
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
                         nodeComparator);
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
    String key = normalizeKey(fullKey);
    boolean deleted = (flags & ITable.NOTIFY_DELETE) != 0;
    List<String> pathElements = Stream.of(key.split("/"))
                                      .filter(s -> !s.isEmpty())
                                      .collect(Collectors.toList());
    TreeItem<NetworkTableEntry> current = root;
    TreeItem<NetworkTableEntry> parent;
    StringBuilder currentKey = new StringBuilder();
    // Add, remove or update nodes in the tree as necessary
    for (int i = 0; i < pathElements.size(); i++) {
      String pathElement = pathElements.get(i);
      currentKey.append('/').append(pathElement);
      parent = current;
      current = current.getChildren().stream()
                       .filter(item -> item.getValue().getKey().equals(currentKey.toString()))
                       .findFirst()
                       .orElse(null);
      if (deleted) {
        if (current == null) {
          // Nothing to remove
          break;
        } else if (i == pathElements.size() - 1) {
          // Remove the final node
          parent.getChildren().remove(current);
        }
      } else if (i == pathElements.size() - 1) {
        // At the end
        if (current == null) {
          // Newly added value, create a tree item for it
          current = new TreeItem<>(new NetworkTableEntry(key, asString(value)));
          parent.getChildren().add(current);
        } else {
          // The value updated, so just update the previous node
          current.getValue().setValue(asString(value));
        }
      } else if (current == null) {
        // It's a branch (subtable); expand it
        current = new TreeItem<>(new NetworkTableEntry(currentKey.toString(), ""));
        current.setExpanded(true);
        parent.getChildren().add(current);
      }
    }
    sort();
    Platform.runLater(this::refreshTableView);
  }

  private void refreshTableView() {
    keyColumn.setVisible(false);
    keyColumn.setVisible(true);
  }

  private String asString(Object o) {
    if (o instanceof double[]) {
      return Arrays.toString((double[]) o);
    }
    if (o instanceof String[]) {
      return Arrays.toString((String[]) o);
    }
    if (o instanceof boolean[]) {
      return Arrays.toString((boolean[]) o);
    }
    return o.toString();
  }

}
