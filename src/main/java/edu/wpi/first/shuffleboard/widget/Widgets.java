package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.sources.DataSource;
import javafx.fxml.FXMLLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for keeping track of known widgets.
 */
public final class Widgets {

  // map descriptions to suppliers to avoid muddling with view internals if they were singletons
  private static final Map<WidgetDescription, UnsafeSupplier<Widget<?>>> widgets = new HashMap<>();

  @FunctionalInterface
  private interface UnsafeSupplier<T> {
    T create() throws Exception;
  }

  private Widgets() {
    // Utility class, prevent instantiation
  }

  /**
   * Registers a widget class.
   */
  static void register(Class<Widget<?>> widgetClass) {
    if (!widgetClass.isAnnotationPresent(Description.class)) {
      throw new InvalidWidgetException(
          "No description present on widget class " + widgetClass.getName());
    }
    Description description = widgetClass.getAnnotation(Description.class);
    validate(description);
    ParametrizedController controller = widgetClass.getAnnotation(ParametrizedController.class);
    boolean fxml = controller != null;
    UnsafeSupplier<Widget<?>> supplier;
    if (fxml) {
      supplier = () -> {
        FXMLLoader loader = new FXMLLoader(widgetClass.getResource(controller.value()));
        loader.load();
        return loader.getController();
      };
    } else {
      supplier = widgetClass::newInstance;
    }
    widgets.put(new WidgetDescription(description), supplier);
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
    if (widgets.keySet().stream()
               .map(WidgetDescription::getName)
               .anyMatch(description.name()::equals)) {
      throw new InvalidWidgetException(
          "A widget already exists with the same name: " + description.name());
    }
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
  @SuppressWarnings("unchecked")
  public static <T> Optional<Widget<T>> createWidget(String name, DataSource<T> source) {
    return widgets.entrySet().stream()
                  .filter(e -> e.getKey().getName().equals(name))
                  .map(e -> (Widget<T>) create(e.getValue()))
                  .filter(Objects::nonNull)
                  .peek(w -> w.setSource(source))
                  .peek(Widget::initialize)
                  .findFirst();
  }

  @SuppressWarnings("unchecked")
  private static <T> Widget<T> create(UnsafeSupplier<Widget<?>> supplier) {
    try {
      return (Widget<T>) supplier.create();
    } catch (Exception e) {
      // TODO log
      return null;
    }
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
    return widgets.keySet().stream()
                  .filter(d -> d.getDataTypes().contains(DataType.All)
                      || d.getDataTypes().contains(type))
                  .map(WidgetDescription::getName)
                  .collect(Collectors.toList());
  }

}
