package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.DataSource;

import java.util.stream.Stream;

/**
 * A widget is a UI element that displays data from {@link DataSource data sources} and has the ability to
 * modify that data. It is up to the widget implementation to modify the backing data; some widgets
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
 * <li>A name</li>
 * <li>Supported data types</li>
 * <li>Data source(s)</li>
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
 */
public interface Widget extends Component, Sourced {

  @Override
  default Stream<Component> allComponents() {
    return Stream.of(this);
  }
}
