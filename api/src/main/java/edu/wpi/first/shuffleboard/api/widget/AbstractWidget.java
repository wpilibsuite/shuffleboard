package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A partial implementation of {@link Widget} that implements property methods. This also has a method
 * {@link #exportProperties(Property[]) exportProperties} that allows subclasses to easily add properties.
 */
public abstract class AbstractWidget implements Widget {

  protected final ObservableList<DataSource> sources = FXCollections.observableArrayList();

  private final StringProperty title = new SimpleStringProperty(this, "title", "");

  private final ObservableList<Property<?>> properties = FXCollections.observableArrayList();

  protected AbstractWidget() {
    sources.addListener((InvalidationListener) __ -> {
      if (sources.size() == 1) {
        setTitle(sources.get(0).getName());
      } else {
        setTitle(getName() + " (" + sources.size() + " sources)");
      }
    });
  }

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
  public StringProperty titleProperty() {
    return title;
  }

  @Override
  public final ObservableList<Property<?>> getProperties() {
    return properties;
  }

  @Override
  public final ObservableList<DataSource> getSources() {
    return sources;
  }

  @Override
  public void addSource(DataSource source) throws IncompatibleSourceException {
    if (!getDataTypes().contains(source.getDataType())) {
      throw new IncompatibleSourceException(getDataTypes(), source.getDataType());
    }
    sources.add(source);
  }

}
