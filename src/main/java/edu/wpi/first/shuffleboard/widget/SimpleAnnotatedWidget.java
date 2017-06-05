package edu.wpi.first.shuffleboard.widget;

import com.google.common.collect.ImmutableSet;
import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.IncompatibleSourceException;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import org.fxmisc.easybind.EasyBind;

import java.util.Objects;
import java.util.Set;

/**
 * A widget is a UI element that displays data from a {@link DataSource} and has the ability to
 * modify that data. It is up to the widget implementation to modify the backing data; some sources
 * (for example, a graph widget) may be read-only. Simple widgets can display singular data points
 * (eg some text, a number, a boolean value, etc); composite widgets display complex data like
 * subsystems or sensors.
 *
 * <p>Widget subclasses should have the {@link Description @Description} annotation that specifies
 * the name, summary, and supported data types. Additionally, subclasses that are FXML controllers
 * must have the {@link ParametrizedController @ParametrizedController} annotation that specifies
 * the location of the FXML file relative to that class.
 *
 * <p>Widgets have a few important properties:
 * <ul>
 * <li>Name</li>
 * <li>Collection of supported data types</li>
 * <li>A data source</li>
 * <li>A view</li>
 * </ul>
 *
 * <p>The name of a widget should be unique much like a class or package name. It's a
 * human-readable identifier that allows users to easily understand what widgets are being
 * displayed, and which widgets can display which data types.
 *
 * <p>All widgets can display some arbitrary amount and combination of {@link DataType data types}.
 * By specifying these data types, the widget declares that it can <i>always</i> handle that kind of
 * data. For example, a "Text Display" widget could hypothetically support String, Number, and
 * Boolean. This means that any data of type String, Number, or Boolean could be displayed with a
 * "Text Display" widget.
 *
 * <p>The data source provides an easy way for a widget to get information about the data it
 * handles. More information about data sources can be found {@link DataSource here}.
 *
 * <p>A widget has a single method {@link #getView()} that returns a JavaFX pane that contains all
 * the UI components that display information about the widget.
 *
 * @param <T> the type of data the widget supports. For composite widgets, this is always
 *            {@link ObservableMap ObservableMap&lt;String, Object&gt;}.
 */
public abstract class SimpleAnnotatedWidget<T> implements Widget {

  protected final SimpleObjectProperty<DataSource<T>> source
      = new SimpleObjectProperty<>(DataSource.none());

  /**
   * The property for this widgets data. This is the preferred way to get the current value of the
   * data source because it will update whenever the source is modified.
   */
  protected final Property<T> data
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
  public ObservableList<Property<?>> getProperties() {
    return properties;
  }

}
