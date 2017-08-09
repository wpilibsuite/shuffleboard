package edu.wpi.first.shuffleboard.app.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class for keeping track of known widgets.
 */
public final class Widgets {

  private static Map<String, WidgetType> widgets = new TreeMap<>();
  private static Map<DataType, WidgetType> defaultWidgets = new HashMap<>();
  private static boolean loadedStockWidgets = false;

  private Widgets() {
    // Utility class, prevent instantiation
  }

  /**
   * Discovers and loads stock and custom widgets. Stock widgets are only loaded once since they
   * never change; custom widgets are unloaded (if they were changed) and are reloaded with the
   * newest version.
   */
  public static void discover() {
    if (!loadedStockWidgets) {
      StockWidgets.init();
      loadedStockWidgets = true;
    }
    // TODO discover custom widgets
  }

  /**
   * Registers a widget class.
   */
  static void register(Class<Widget> widgetClass) {
    if (!widgetClass.isAnnotationPresent(Description.class)) {
      throw new InvalidWidgetException(
          "No description present on widget class " + widgetClass.getName());
    }
    Description description = widgetClass.getAnnotation(Description.class);
    validate(description);
    ParametrizedController controller = widgetClass.getAnnotation(ParametrizedController.class);

    WidgetType widgetType = new AbstractWidgetType(description) {
      @Override
      public Widget get() {
        boolean fxml = controller != null;

        try {
          if (fxml) {
            FXMLLoader loader = new FXMLLoader(widgetClass.getResource(controller.value()));
            loader.load();
            return loader.getController();
          } else {
            return widgetClass.newInstance();
          }
        } catch (IllegalAccessException | IOException | InstantiationException e) {
          Logger.getLogger("Widgets").log(Level.WARNING, "error creating widget", e);
          return null;
        }
      }
    };

    widgets.put(widgetType.getName(), widgetType);
  }

  /**
   * Validates a widget description.
   *
   * @param description the description to validate
   * @throws InvalidWidgetException if the widget is invalid
   */
  private static void validate(Description description) throws InvalidWidgetException {
    if (description.name().isEmpty()) {
      throw new InvalidWidgetException("No name specified for the widget");
    }
    if (widgets.containsKey(description.name())) {
      throw new InvalidWidgetException(
              "A widget already exists with the same name: " + description.name());
    }
  }

  public static Collection<WidgetType> allWidgets() {
    return widgets.values();
  }

  /**
   * Tries to create a widget from a known widget with the given name. If successful, the widgets
   * data source will be set to the given one.
   *
   * @param name   the name of the widget to create
   * @param source the data source for the widget to use
   * @return an optional containing the created view, or an empty optional if no widget could
   *         be created
   */
  public static <T> Optional<Widget> createWidget(String name, DataSource<T> source) {
    Optional<Widget> widget = typeFor(name).map(WidgetType::get);
    widget.ifPresent(w -> w.setSource(source));
    return widget;
  }

  /**
   * Retrieve the factory for this widget using its unique name.
   * @param name the globally unique name of the widget in question
   * @return a WidgetType to create widgets of the same class
   */
  public static Optional<WidgetType> typeFor(String name) {
    return Optional.ofNullable(widgets.get(name));
  }

  private static Set<WidgetType> getWidgetsForType(DataType type) {
    return widgets.values().stream()
            .filter(d -> d.getDataTypes().contains(DataTypes.All)
                         || d.getDataTypes().contains(type))
            .collect(Collectors.toSet());
  }

  /**
   * Gets the names of all the possible widget that can display the given type, sorted alphabetically. A widget can be
   * created for these with {@link #createWidget(String, DataSource) createWidget}.
   *
   * @param type the type of data to get possible widgets for.
   * @return an alphabetically sorted list containing the names of all known widgets that can display data of the
   *         given type
   */
  public static List<String> widgetNamesForType(DataType type) {
    return getWidgetsForType(type)
        .stream()
        .map(WidgetType::getName)
        .sorted()
        .collect(Collectors.toList());
  }

  /**
   * Sets the default widget to use for a given data type.
   *
   * @param dataType   the type to set the default widget for
   * @param widgetType the type of widget to set as the default
   */
  public static void setDefaultWidget(DataType dataType, WidgetType widgetType) {
    defaultWidgets.put(dataType, widgetType);
  }

  /**
   * Sets the default widget to use for a given data type. Note that a widget must have already been registered
   * with the given name for this method to have an affect.
   *
   * @param dataType   the type to set the default widget for
   * @param widgetName the name of the widget to use as the default
   */
  public static void setDefaultWidget(DataType dataType, String widgetName) {
    WidgetType widgetType = widgets.get(widgetName);
    if (widgetName != null) {
      setDefaultWidget(dataType, widgetType);
    }
  }

  /**
   * Gets the name of the default widget for the given data type, or {@link Optional#empty()} if there is no default
   * widget for that type.
   */
  public static Optional<String> defaultWidgetNameFor(DataType type) {
    return Optional.ofNullable(defaultWidgets.get(type)).map(WidgetType::getName);
  }

  /**
   * Gets the name of a widget that can handle data of the given type. If a default widget has been set for that type,
   * the name of the default widget is returned; otherwise, the name of the first widget returned by
   * {@link #widgetNamesForType(DataType)} is used.
   */
  public static Optional<String> pickWidgetNameFor(DataType type) {
    Optional<String> defaultName = defaultWidgetNameFor(type);
    if (defaultName.isPresent()) {
      return defaultName;
    } else {
      List<String> names = widgetNamesForType(type);
      if (names.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(names.get(0));
      }
    }
  }

  /**
   * Gets the names of all the possible widgets than can display the data in a given source.
   */
  public static List<String> widgetNamesForSource(DataSource<?> source) {
    return widgetNamesForType(source.getDataType());
  }

}
