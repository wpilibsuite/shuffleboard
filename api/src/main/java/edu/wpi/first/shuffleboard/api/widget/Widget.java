package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.DataSource;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import javafx.beans.property.Property;
import javafx.collections.ObservableMap;

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
public interface Widget extends Component, Sourced {

  /**
   * Gets an unmodifiable copy of this widgets supported data types.
   */
  Set<DataType> getDataTypes();

  /**
   * Gets the user-configurable properties for this widget.
   */
  List<Property<?>> getProperties();

  @Override
  default Stream<Widget> allWidgets() {
    return Stream.of(this);
  }
}
