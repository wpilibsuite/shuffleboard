package edu.wpi.first.shuffleboard.dnd;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TreeTableRow;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

/**
 * A tree table row that represents a data source. This class has a property for a converter
 * to use to convert the entry in the row into a transferable data source so the source the
 * row represents can be dragged and dropped into a UI component that can accept data sources.
 */
public class SourceRow<T> extends TreeTableRow<T> {

  private final Property<EntryConverter<? super T>> converter =
      new SimpleObjectProperty<>(this, "entryConverter", null);

  /**
   * Converts an entry in a source row into a transferable data source.
   *
   * @param <T> the type of entry in the source row
   */
  @FunctionalInterface
  public interface EntryConverter<T> {
    DataSourceTransferable convert(T entry);
  }

  /**
   * Creates a new source row. The entry converter must be set later with
   * {@link #setEntryConverter(EntryConverter) setEntryConverter}.
   */
  public SourceRow() {
    setOnDragDetected(event -> {
      startDrag();
      event.consume();
    });
  }

  private void startDrag() {
    if (isEmpty()) {
      // Nothing to drag
      return;
    }
    if (getEntryConverter() == null) {
      // No converter, can't drag anything
      return;
    }
    T entry = getTreeItem().getValue();
    Dragboard dragboard = startDragAndDrop(TransferMode.COPY_OR_MOVE);
    ClipboardContent content = new ClipboardContent();
    content.put(DataFormats.source, getEntryConverter().convert(entry));
    dragboard.setContent(content);
  }

  public final Property<EntryConverter<? super T>> entryConverterProperty() {
    return converter;
  }

  public final EntryConverter<? super T> getEntryConverter() {
    return converter.getValue();
  }

  /**
   * Sets the converter to use to convert the entry in this row into a transferable data source. If
   * null (which it is by default), this row will not be draggable.
   *
   * @param entryConverter the converter to use
   */
  public final void setEntryConverter(EntryConverter<? super T> entryConverter) {
    this.converter.setValue(entryConverter);
  }

}
