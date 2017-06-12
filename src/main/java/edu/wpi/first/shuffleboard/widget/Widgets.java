package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.sources.DataSource;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class for keeping track of known widgets.
 */
public final class Widgets {

  private static Map<String, WidgetType> widgets = new HashMap<>();
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
            .filter(d -> d.getDataTypes().contains(DataType.All)
                         || d.getDataTypes().contains(type))
            .collect(Collectors.toSet());
  }

  /**
   * Gets the names of all the possible widget that can display the given type. A widget can be
   * created for these with {@link #createWidget(String, DataSource) createWidget}.
   *
   * @param type the type of data to get possible widgets for.
   * @return a list containing the names of all known widgets that can display data of the
   *         given type
   */
  public static List<String> widgetNamesForType(DataType type) {
    return getWidgetsForType(type).stream().map(WidgetType::getName).collect(Collectors.toList());
  }

  /**
   * Gets the names of all the possible widgets than can display the data in a given source.
   */
  public static List<String> widgetNamesForSource(DataSource<?> source) {
    return widgetNamesForType(source.getDataType());
  }

}
