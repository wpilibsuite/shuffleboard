package edu.wpi.first.shuffleboard.widget;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.IncompatibleSourceException;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.fxmisc.easybind.EasyBind;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class SimpleAnnotatedWidget<T> implements Widget {

  protected final SimpleObjectProperty<DataSource<T>> source
      = new SimpleObjectProperty<>(DataSource.none());

  /**
   * The property for this widgets data. This is the preferred way to get the current value of the
   * data source because it will update whenever the source is modified.
   */
  private final Property<T> data
      = EasyBind.monadic(source).selectProperty(DataSource::dataProperty);

  private final Property<String> sourceName = new SimpleStringProperty(this, "sourceName", "");
  private final ObservableList<Property<?>> properties = FXCollections.observableArrayList();
  protected final Description description = getClass().getAnnotation(Description.class);

  // Getters and setters

  @Override
  public final String getName() {
    return description.name();
  }

  @Override
  public final Set<DataType> getDataTypes() {
    return ImmutableSet.copyOf(description.dataTypes());
  }

  /**
   * Gets the data source that this widget backs. The source is automatically set when the
   * widget is created.
   */
  @Override
  public final DataSource<T> getSource() {
    return source.get();
  }

  /**
   * Bind a source to a widget.
   */
  public final void setSource(DataSource source) throws IncompatibleSourceException {
    Objects.requireNonNull(source, "A widget must have a source");
    if (!getDataTypes().contains(source.getDataType())) {
      throw new IncompatibleSourceException(getDataTypes(), source.getDataType());
    }
    this.source.set(source);
    sourceName.setValue(source.getName());
  }

  public Property<DataSource<T>> sourceProperty() {
    return source;
  }

  protected final Property<T> dataProperty() {
    return data;
  }

  protected final T getData() {
    return data.getValue();
  }

  protected final void setData(T data) {
    this.data.setValue(data);
  }

  public final String getSourceName() {
    return sourceName.getValue();
  }

  public final Property<String> sourceNameProperty() {
    return sourceName;
  }

  /**
   * Exports the given properties so other parts of the app can see the properties of this widget.
   * Not all properties need to (or should be) exported; it should only properties that can be
   * user-configurable. If possible, the view for this widget will allow users to modify the value
   * of each property. For example, a "Number Slider" widget with a slider could have the minimum
   * and maximum values of that slider be configurable by the user.
   */
  protected void exportProperties(Property<?>... properties) {
    this.properties.setAll(properties);
  }

  @Override
  public List<Property<?>> getProperties() {
    return ImmutableList.copyOf(properties);
  }

}
