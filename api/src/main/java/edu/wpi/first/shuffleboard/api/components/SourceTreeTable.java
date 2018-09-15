package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.sources.DataSourceUtils;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;
import edu.wpi.first.shuffleboard.api.util.EqualityUtils;

import java.util.Comparator;
import java.util.List;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

/**
 * A tree table view or displaying hierarchical sources.  Note: the root item <strong>must</strong> be a
 * {@link FilterableTreeItem}. This allows source trees to be searchable.  Not setting the root item to a
 * {@code FilterableTreeItem} will result in exceptions being thrown when items in the tree table are added,
 * removed, or updated.
 *
 * @param <S> the type of the source entries in the tree
 * @param <V> the type of the values of the sources
 */
public class SourceTreeTable<S extends SourceEntry, V> extends TreeTableView<S> {

  /**
   * Compares tree items, branches first.
   */
  public static final Comparator<TreeItem<? extends SourceEntry>> branchesFirst
      = (o1, o2) -> Boolean.compare(o1.isLeaf(), o2.isLeaf());

  /**
   * Compares tree items alphabetically.
   */
  public static final Comparator<TreeItem<? extends SourceEntry>> alphabetical
      = Comparator.comparing(item -> item.getValue().getViewName(), AlphanumComparator.INSTANCE);

  private final ObjectProperty<SourceType> sourceType = new SimpleObjectProperty<>(this, "sourceType", null);

  private final TreeTableColumn<S, String> keyColumn = new TreeTableColumn<>("Name");
  private final TreeTableColumn<S, V> valueColumn = new TreeTableColumn<>("Value");

  /**
   * Creates a new source tree table. It comes pre-populated with a key and a value column.
   */
  public SourceTreeTable() {
    keyColumn.prefWidthProperty().bind(widthProperty().divide(2).subtract(2));
    valueColumn.prefWidthProperty().bind(widthProperty().divide(2).subtract(2));

    keyColumn.setCellValueFactory(
        f -> new ReadOnlyStringWrapper(getEntryForCellData(f).getViewName()));
    valueColumn.setCellValueFactory(
        f -> new ReadOnlyObjectWrapper(getEntryForCellData(f).getValueView()));
    Label placeholder = new Label("No data available");
    setPlaceholder(placeholder);

    getColumns().addAll(keyColumn, valueColumn);
  }

  /**
   * Updates a source entry in this tree table, creating branches as needed.
   *
   * @param entry the entry to update
   */
  public void updateEntry(S entry) {
    makeBranches(entry, false);
  }

  /**
   * Removes a source entry from this tree table, as well as all child entries and empty parents.
   *
   * @param entry the entry to remove
   */
  public void removeEntry(S entry) {
    makeBranches(entry, true);
  }

  /**
   * Creates, updates, or deletes tree nodes in the network table view.
   *
   * @param entry   the entry that should be updated
   * @param deleted {@code true} if the entry was deleted, {@code false} otherwise
   */
  private void makeBranches(S entry, boolean deleted) {
    final SourceType sourceType = getSourceType();
    String name = entry.getName();
    List<String> hierarchy = DataSourceUtils.getHierarchy(name);
    FilterableTreeItem<S> current = (FilterableTreeItem<S>) getRoot();
    FilterableTreeItem<S> parent = current;
    boolean structureChanged = false;

    // Get the appropriate node for the value, creating branches as needed
    // Skip the first path in the hierarchy; it's always the root
    for (int i = 1; i < hierarchy.size(); i++) {
      String path = hierarchy.get(i);
      parent = current;
      FilterableTreeItem<S> found = null;
      for (TreeItem<S> item : current.getAllChildren()) {
        if (item.getValue().getName().equals(path)) {
          found = (FilterableTreeItem<S>) item;
          break;
        }
      }
      current = found;
      if (current == null && deleted) {
        // Done
        break;
      } else if (!deleted && i < hierarchy.size() - 1 && current == null) {
        // It's a branch (subtable); expand it
        S newEntry = (S) sourceType.createSourceEntryForUri(sourceType.toUri(path));
        current = new FilterableTreeItem<>(newEntry);
        current.setExpanded(true);
        parent.getAllChildren().add(current);
        structureChanged = true;
      }
    }

    // Remove, update, or create the value node as needed
    if (deleted) {
      if (current != null) {
        parent.getAllChildren().remove(current);

        // Remove empty subtrees
        if (parent.getAllChildren().isEmpty()) {
          FilterableTreeItem<S> item = (FilterableTreeItem<S>) parent.getParent();
          while (item != null) {
            item.getAllChildren().remove(parent);
            parent = item;
            item = (FilterableTreeItem<S>) item.getParent();
          }
        }
        structureChanged = true;
      }
    } else {
      if (current == null) {
        // Newly added value, create a tree item for it
        current = new FilterableTreeItem<>(entry);
        current.setExpanded(true);
        parent.getAllChildren().add(current);
        structureChanged = true;
      } else if (EqualityUtils.isDifferent(current.getValue().getValue(), entry.getValue())) {
        // The value updated, so just update the existing node
        current.setValue(entry);
      }
    }
    if (structureChanged) {
      ((FilterableTreeItem<S>) getRoot()).sortChildren();
    }
  }

  protected static <T> T getEntryForCellData(TreeTableColumn.CellDataFeatures<T, ?> features) {
    return features.getValue().getValue();
  }

  public final SourceType getSourceType() {
    return sourceType.get();
  }

  public final ObjectProperty<SourceType> sourceTypeProperty() {
    return sourceType;
  }

  public final void setSourceType(SourceType sourceType) {
    this.sourceType.set(sourceType);
  }

}
