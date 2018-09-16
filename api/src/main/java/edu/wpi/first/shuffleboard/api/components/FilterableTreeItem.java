package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;

import org.fxmisc.easybind.EasyBind;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TreeItem;

public class FilterableTreeItem<T extends SourceEntry> extends TreeItem<T> {

  private static final Comparator<TreeItem<? extends SourceEntry>> comparator =
      SourceTreeTable.branchesFirst
          .thenComparing(SourceTreeTable.alphabetical);

  private final Predicate<TreeItem<T>> always = item -> {
    if (item instanceof FilterableTreeItem) {
      ((FilterableTreeItem<T>) item).setPredicate(TreeItemPredicate.always());
    }
    return true;
  };

  private final ObservableList<TreeItem<T>> sourceList = FXCollections.observableArrayList();
  private final List<TreeItem<T>> sourceCopy = new ArrayList<>();
  private final FilteredList<TreeItem<T>> filteredList = new FilteredList<>(sourceList);
  private final SortedList<TreeItem<T>> sortedList = new SortedList<>(filteredList, comparator);

  private final ObjectProperty<TreeItemPredicate<T>> predicate = new SimpleObjectProperty<>(TreeItemPredicate.always());

  @SuppressWarnings("JavadocMethod")
  public FilterableTreeItem(T value) {
    super(value);
    EasyBind.listBind(sourceCopy, sourceList);
    sortedList.setComparator(comparator);
    filteredList.predicateProperty().bind(
        EasyBind.monadic(predicate)
            .map(this::createListFilter)
            .orElse(always));

    setHiddenFieldChildren(sortedList);
  }

  protected void setHiddenFieldChildren(ObservableList<TreeItem<T>> list) {
    try {
      Field children = TreeItem.class.getDeclaredField("children");
      Field childrenListener = TreeItem.class.getDeclaredField("childrenListener");
      children.setAccessible(true);
      childrenListener.setAccessible(true);
      children.set(this, list);
      list.addListener((ListChangeListener<? super TreeItem<T>>) childrenListener.get(this));
    } catch (ReflectiveOperationException e) {
      throw new AssertionError("Could not modify internal fields", e);
    }
  }

  public final ObservableList<TreeItem<T>> getAllChildren() {
    return sourceList;
  }

  public final TreeItemPredicate<T> getPredicate() {
    return predicate.get();
  }

  public final ObjectProperty<TreeItemPredicate<T>> predicateProperty() {
    return predicate;
  }

  public final void setPredicate(TreeItemPredicate<T> predicate) {
    this.predicate.set(predicate);
  }

  /**
   * Recursively sorts all children of this item. Children are sorted by branches first, then alphanumerically.
   */
  public void sortChildren() {
    // Forces the list to re-sort
    sortedList.setComparator(null);
    sortedList.setComparator(comparator);
    sourceList.stream()
        .flatMap(TypeUtils.castStream(FilterableTreeItem.class))
        .forEach(FilterableTreeItem::sortChildren);
  }

  protected Predicate<TreeItem<T>> createListFilter(TreeItemPredicate<T> predicate) {
    if (predicate == TreeItemPredicate.ALWAYS) {
      return always;
    }
    return child -> {
      if (child instanceof FilterableTreeItem) {
        ((FilterableTreeItem<T>) child).setPredicate(predicate);
      }
      if (predicate == null) {
        return true;
      }
      if (!child.getChildren().isEmpty()) {
        return true;
      }
      return predicate.test(child.getParent(), child.getValue());
    };
  }
}
