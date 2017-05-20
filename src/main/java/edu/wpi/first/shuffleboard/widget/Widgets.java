package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.sources.DataSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Utility class for keeping track of known widgets.
 */
public final class Widgets {

  // map descriptions to init functions to avoid muddling with view internals if they were singletons
  private static final Map<WidgetDescription, Consumer<? extends Widget<?>>> widgets = new HashMap<>();

  // package-private; should only be used by simpleWidget in Widget.java
  static <T> void register(Consumer<Widget<T>> init) {
    // Create a dummy widget to validate the function and extract metadata from it (name, sizes, etc)
    Widget<T> dummy = new Widget<>();
    init.accept(dummy);
    validate(dummy);
    widgets.put(new WidgetDescription(dummy), init);
  }

  /**
   * Validates a widget. The initialization function for the widget will not be added if a widget constructed
   * with it cannot pass validation.
   *
   * @param widget the widget to validate
   * @throws InvalidWidgetException if the widget is invalid
   */
  private static void validate(Widget<?> widget) throws InvalidWidgetException {
    if (widget.getName().isEmpty()) {
      throw new InvalidWidgetException("No name specified for the widget");
    }
    if (widgets.keySet().stream().map(WidgetDescription::getName).anyMatch(widget.getName()::equals)) {
      throw new InvalidWidgetException("A widget already exists with the same name: " + widget.getName());
    }
    if (widget.getViews().isEmpty()) {
      throw new InvalidWidgetException("No views specified for " + widget.getName());
    }
    if (widget.getViews().keySet().stream().noneMatch(widget.getPreferredSize()::equals)) {
      throw new InvalidWidgetException("The preferred size doesn't have an associated widget");
    }
  }

  /**
   * Tries to create a widget from a known widget with the given name. If successful, the widgets data source
   * will be set to the given one.
   *
   * @param name   the name of the widget to create
   * @param source the data source for the widget to use
   * @return an optional containing the created view, or an empty optional if no widget could be created
   */
  @SuppressWarnings("unchecked")
  public static <T> Optional<Widget<T>> createWidget(String name, DataSource<T> source) {
    return widgets.entrySet().stream()
                  .filter(e -> name.equals(e.getKey().getName()))
                  .map(e -> {
                    Widget w = new Widget();
                    w.setSource(source);
                    w.configure(e.getValue());
                    return w;
                  })
                  .map(v -> (Widget<T>) v)
                  .findFirst();
  }

  /**
   * Gets the names of all the possible widget that can display the given type. A widget can be created for these
   * with {@link #createWidget(String, DataSource) createWidget}.
   *
   * @param type the type of data to get possible widgets for.
   * @return a list containing the names of all known widgets that can display data of the given type
   */
  public static List<String> widgetNamesForType(DataType type) {
    return widgets.keySet().stream()
                  .filter(d -> d.getDataTypes().contains(DataType.All) || d.getDataTypes().contains(type))
                  .map(WidgetDescription::getName)
                  .collect(Collectors.toList());
  }

}