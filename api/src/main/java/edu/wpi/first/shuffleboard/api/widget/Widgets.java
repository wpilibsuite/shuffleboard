package edu.wpi.first.shuffleboard.api.widget;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.Registry;
import edu.wpi.first.shuffleboard.api.util.TestUtils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.fxml.FXMLLoader;

import static java.util.Objects.requireNonNull;

/**
 * Utility class for keeping track of known widgets.
 */
public class Widgets extends Registry<Class<? extends Widget>> {

  // TODO replace with DI eg Guice
  private static Widgets defaultInstance = new Widgets();

  private final Map<Class<? extends Widget>, WidgetType> registeredWidgets = new HashMap<>();
  private final Map<String, WidgetType> widgets = new TreeMap<>();
  private final Map<DataType, WidgetType> defaultWidgets = new HashMap<>();
  private final WeakHashMap<Widget, Widget> activeWidgets = new WeakHashMap<>();

  /**
   * Gets the default widget registry.
   */
  public static Widgets getDefault() {
    return defaultInstance;
  }

  /**
   * Sets the default instance to use. <strong>This may only be called from tests</strong>.
   *
   * @throws IllegalStateException if not called from a test
   */
  @VisibleForTesting
  public static void setDefault(Widgets instance) {
    TestUtils.assertRunningFromTest();
    defaultInstance = instance;
  }

  @Override
  public void register(Class<? extends Widget> widgetClass) {
    registerHelper(widgetClass);
  }

  private <T extends Widget> void registerHelper(Class<T> widgetClass) {
    requireNonNull(widgetClass, "widgetClass");
    if (isRegistered(widgetClass)) {
      throw new IllegalArgumentException("Widget class " + widgetClass.getName() + " is already registered");
    }
    if (!widgetClass.isAnnotationPresent(Description.class)) {
      throw new InvalidWidgetException(
          "No description present on widget class " + widgetClass.getName());
    }
    Description description = widgetClass.getAnnotation(Description.class);
    validate(description);

    WidgetType widgetType = new AbstractWidgetType(description) {
      @Override
      public Widget get() {
        return viewFor(widgetClass).orElseGet(() -> {
          try {
            return widgetClass.newInstance();
          } catch (InstantiationException | IllegalAccessException e) {
            Logger.getLogger("Widgets").log(Level.WARNING, "error creating widget", e);
            return null;
          }
        });
      }
    };

    registeredWidgets.put(widgetClass, widgetType);
    widgets.put(widgetType.getName(), widgetType);
    addItem(widgetClass);
  }

  @Override
  public void unregister(Class<? extends Widget> widgetClass) {
    WidgetType widgetType = registeredWidgets.get(widgetClass);
    widgets.entrySet().stream()
        .filter(e -> e.getValue() == widgetType)
        .map(Map.Entry::getKey)
        .findFirst()
        .ifPresent(widgets::remove);
    defaultWidgets.entrySet().stream()
        .filter(e -> e.getValue() == widgetType)
        .map(Map.Entry::getKey)
        .findFirst()
        .ifPresent(defaultWidgets::remove);
    registeredWidgets.remove(widgetClass);
    removeItem(widgetClass);
  }

  public static <T> Optional<T> viewFor(Class<T> annotatedClass) {
    ParametrizedController controller = annotatedClass.getAnnotation(ParametrizedController.class);

    if (controller != null) {
      try {
        FXMLLoader loader = new FXMLLoader(annotatedClass.getResource(controller.value()));
        loader.load();
        return Optional.of(loader.getController());
      } catch (IOException e) {
        Logger.getLogger("Widgets").log(Level.WARNING, "error creating widget", e);
      }
    }

    return Optional.empty();
  }

  /**
   * Validates a widget description.
   *
   * @param description the description to validate
   *
   * @throws InvalidWidgetException if the widget is invalid
   */
  private void validate(Description description) throws InvalidWidgetException {
    if (description.name().isEmpty()) {
      throw new InvalidWidgetException("No name specified for the widget");
    }
    if (widgets.containsKey(description.name())) {
      throw new InvalidWidgetException(
          "A widget already exists with the same name: " + description.name());
    }
  }

  public Collection<WidgetType> allWidgets() {
    return widgets.values();
  }

  /**
   * Tries to create a widget from a known widget with the given name. If successful, the widgets
   * data source will be set to the given one.
   *
   * @param name   the name of the widget to create
   * @param source the data source for the widget to use
   *
   * @return an optional containing the created view, or an empty optional if no widget could
   *         be created
   */
  public <T> Optional<Widget> createWidget(String name, DataSource<T> source) {
    Optional<Widget> widget = typeFor(name).map(WidgetType::get);
    widget.ifPresent(w -> activeWidgets.put(w, w));
    widget.ifPresent(w -> w.setSource(source));
    return widget;
  }

  /**
   * Gets a list of the active widgets in the application.
   *
   * <p><strong>Do not keep references to elements in this list.</strong> It prevents garbage collection of
   * widget instances.
   */
  public List<Widget> getActiveWidgets() {
    // Use a copy; don't want elements in the list to be removed by GC while someone's using it
    return ImmutableList.copyOf(activeWidgets.keySet());
  }

  /**
   * Retrieve the factory for this widget using its unique name.
   *
   * @param name the globally unique name of the widget in question
   *
   * @return a WidgetType to create widgets of the same class
   */
  public Optional<WidgetType> typeFor(String name) {
    return Optional.ofNullable(widgets.get(name));
  }

  private Set<WidgetType> getWidgetsForType(DataType type) {
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
   *
   * @return an alphabetically sorted list containing the names of all known widgets that can display data of the
   *         given type
   */
  public List<String> widgetNamesForType(DataType type) {
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
  public void setDefaultWidget(DataType dataType, WidgetType widgetType) {
    defaultWidgets.put(dataType, widgetType);
  }

  /**
   * Sets the default widget to use for a given data type.
   *
   * @param dataType    the type to set the default widget for
   * @param widgetClass the class of the widget to set as the default
   *
   * @throws IllegalArgumentException if the widget has not been registered
   */
  public void setDefaultWidget(DataType<?> dataType, Class<? extends Widget> widgetClass) {
    if (!registeredWidgets.containsKey(widgetClass)) {
      throw new IllegalArgumentException("Widget class " + widgetClass.getName() + " has not been registered");
    }
    setDefaultWidget(dataType, registeredWidgets.get(widgetClass));
  }

  /**
   * Sets the default widget to use for a given data type. Note that a widget must have already been registered
   * with the given name for this method to have an affect.
   *
   * @param dataType   the type to set the default widget for
   * @param widgetName the name of the widget to use as the default
   */
  public void setDefaultWidget(DataType dataType, String widgetName) {
    WidgetType widgetType = widgets.get(widgetName);
    if (widgetName != null) {
      setDefaultWidget(dataType, widgetType);
    }
  }

  /**
   * Gets the name of the default widget for the given data type, or {@link Optional#empty()} if there is no default
   * widget for that type.
   */
  public Optional<String> defaultWidgetNameFor(DataType type) {
    return Optional.ofNullable(defaultWidgets.get(type)).map(WidgetType::getName);
  }

  /**
   * Gets the name of a widget that can handle data of the given type. If a default widget has been set for that type,
   * the name of the default widget is returned; otherwise, the name of the first widget returned by
   * {@link #widgetNamesForType(DataType)} is used.
   */
  public Optional<String> pickWidgetNameFor(DataType type) {
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
  public List<String> widgetNamesForSource(DataSource<?> source) {
    return widgetNamesForType(source.getDataType());
  }
}
