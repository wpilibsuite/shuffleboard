package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;

/**
 * A partial implementation of {@code Widget} that only has a single source.
 */
public abstract class SingleSourceWidget extends AbstractWidget {

  protected final ObjectProperty<DataSource> source = new SimpleObjectProperty<>(this, "source", DataSource.none());

  /**
   * Instantiates a new single source widget. This automatically registers listeners to make the
   * {@link #source} and {@link #sources} properties stay in sync such that both properties will
   * have the same, single data source object.
   */
  public SingleSourceWidget() {
    // Bidirectional binding to make the sources list act like a single-element wrapper around
    // the source property
    source.addListener((__, oldSource, newSource) -> sources.setAll(newSource));

    sources.addListener(new ListChangeListener<DataSource>() {
      @Override
      public void onChanged(Change<? extends DataSource> c) {
        while (c.next()) {
          if (c.wasAdded()) {
            var added = c.getAddedSubList();
            if (!added.isEmpty()) {
              var addedSource = added.get(0);
              if (addedSource != source.get()) {
                source.set(addedSource);
              }
            }
          } else if (c.wasRemoved()) {
            source.set(DataSource.none());
          }
        }
      }
    });
  }

  @Override
  public final void addSource(DataSource source) throws IncompatibleSourceException {
    if (getDataTypes().contains(source.getDataType())) {
      this.source.set(source);
      this.sources.remove(getSource());
      this.sources.setAll(source);
      source.addClient(this);
    } else {
      throw new IncompatibleSourceException(getDataTypes(), source.getDataType());
    }
  }

  public final Property<DataSource> sourceProperty() {
    return source;
  }

  public final DataSource getSource() {
    return source.get();
  }

  public final void setSource(DataSource source) throws IncompatibleSourceException {
    addSource(source);
  }

}
