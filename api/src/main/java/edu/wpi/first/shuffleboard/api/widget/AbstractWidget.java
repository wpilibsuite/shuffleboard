package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A partial implementation of {@link Widget} that implements the source and property methods. This also has a method
 * {@link #exportProperties(Property[]) exportProperties} that allows subclasses to easily add properties.
 */
public abstract class AbstractWidget implements Widget {

  protected final Property<DataSource> source
      = new SimpleObjectProperty<>(this, "source", DataSource.none());
  private final ObservableList<Property<?>> properties = FXCollections.observableArrayList();

  /**
   * Exports the given properties so other parts of the app can see the properties of this widget.
   * Not all properties need to (or should be) exported; it should only properties that can be
   * user-configurable. If possible, the view for this widget will allow users to modify the value
   * of each property. For example, a "Number Slider" widget with a slider could have the minimum
   * and maximum values of that slider be configurable by the user.
   *
   * @param properties the properties to export
   */
  protected final void exportProperties(Property<?>... properties) {
    for (Property<?> property : properties) {
      if (!this.properties.contains(property)) {
        this.properties.add(property);
      }
    }
  }

  @Override
  public final ObservableList<Property<?>> getProperties() {
    return properties;
  }

  @Override
  public Property<DataSource> sourceProperty() {
    return source;
  }

  @Override
  public final DataSource getSource() {
    return source.getValue();
  }

  @Override
  public final void setSource(DataSource source) throws IncompatibleSourceException {
    if (getDataTypes().contains(DataTypes.All) || getDataTypes().contains(source.getDataType())) {
      this.source.setValue(source);
    } else {
      throw new IncompatibleSourceException(getDataTypes(), source.getDataType());
    }
  }

}
