package edu.wpi.first.shuffleboard.widget;

import com.google.common.collect.ImmutableSet;
import edu.wpi.first.shuffleboard.sources.DataSource;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableMap;
import javafx.scene.layout.Pane;

import java.util.Objects;
import java.util.Set;

/**
 * A widget is a UI element that displays data from a {@link DataSource} and has the ability to
 * modify that data. Simple widgets can display singular data points (eg some text, a number,
 * a boolean value, etc); composite widgets display complex data like subsystems or sensors.
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
 * the UI elements that display information about the widget.
 *
 * @param <T> the type of data the widget supports. For composite widgets, this is always
 *            {@link ObservableMap ObservableMap&lt;String, Object&gt;}.
 */
public abstract class Widget<T> {

  protected DataSource<T> source = DataSource.none();
  private final Property<String> sourceName = new SimpleStringProperty(this, "sourceName", "");
  protected final Description description = getClass().getAnnotation(Description.class);


  /**
   * Initializes this widget. This is called after the widget controller is instantiated and the
   * source is set.
   */
  public abstract void initialize();

  /**
   * Gets the JavaFX pane used to display this widget in the UI.
   */
  public abstract Pane getView();

  // Getters and setters

  /**
   * Gets the name of this widget.
   */
  public final String getName() {
    return description.name();
  }

  /**
   * Gets an unmodifiable copy of this widgets supported data types.
   */
  public final Set<DataType> getDataTypes() {
    return ImmutableSet.copyOf(description.dataTypes());
  }

  /**
   * Gets the data source that this widget backs. The source is automatically set when the
   * widget is created.
   */
  public final DataSource<T> getSource() {
    return source;
  }

  /**
   * Sets the source for this widget. This should be called after instantiation but before
   * {@link #initialize()}. This should only be called by internal shuffleboard code.
   */
  final void setSource(DataSource<T> source) {
    this.source = Objects.requireNonNull(source, "A widget must have a source");
    sourceName.setValue(source.getName());
  }

  public final String getSourceName() {
    return sourceName.getValue();
  }

  public final Property<String> sourceNameProperty() {
    return sourceName;
  }

}
