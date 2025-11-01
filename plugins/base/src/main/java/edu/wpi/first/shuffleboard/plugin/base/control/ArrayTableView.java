package edu.wpi.first.shuffleboard.plugin.base.control;


import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Callback;

import java.util.ArrayList;

/**
 * A table view that displays an array {@code T[]} as a table with an index and a value column.
 * @param <T> the type of each item in the array (e.g. {@code Boolean} for {@code Boolean[]})
 */
public class ArrayTableView<T> extends TableView<ArrayTableView.ArrayTableEntry<T>> {
  private final TableColumn<ArrayTableEntry<T>, String> indexCol = new TableColumn<>("Index");
  private final TableColumn<ArrayTableEntry<T>, T> valueCol = new TableColumn<>("Value");

  private final ObservableList<ArrayTableEntry<T>> list = FXCollections.observableArrayList();

  @SuppressWarnings("JavadocMethod")
  public ArrayTableView() {
    super();

    indexCol.setCellValueFactory(p -> new ReadOnlyStringWrapper(String.valueOf(p.getValue().index)));
    indexCol.setResizable(false);
    indexCol.setPrefWidth(50);

    valueCol.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(p.getValue().value));
    valueCol.prefWidthProperty().bind(widthProperty().subtract(50).subtract(2));

    //noinspection unchecked
    getColumns().addAll(indexCol, valueCol);

    setItems(list);

    Label placeholder = new Label("No data available");
    setPlaceholder(placeholder);
  }

  public void setValueCellFactory(
      Callback<TableColumn<ArrayTableEntry<T>, T>, TableCell<ArrayTableEntry<T>, T>> callback
  ) {
    valueCol.setCellFactory(callback);
  }

  /**
   * Convenience method to set the table's items directly, instead of using an {@link ObservableList}.
   */
  public void setItems(T[] items) {
    final var entries = new ArrayList<ArrayTableEntry<T>>();

    for (int i = 0; i < items.length; i++) {
      entries.add(new ArrayTableEntry<>(i, items[i]));
    }

    list.setAll(entries);
  }

  public static class ArrayTableEntry<S> {
    public final int index;
    public final S value;

    public ArrayTableEntry(int index, S value) {
      this.index = index;
      this.value = value;
    }
  }
}
