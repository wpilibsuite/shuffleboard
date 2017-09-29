package edu.wpi.first.shuffleboard.api.widget;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.Registry;
import edu.wpi.first.shuffleboard.api.util.TestUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;

import java.io.IOException;
import java.lang.reflect.Type;
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
import java.util.stream.Stream;

import javafx.fxml.FXMLLoader;

import static java.util.Objects.requireNonNull;

/**
 * Utility class for keeping track of known widgets.
 */
public class Components extends Registry<ComponentType> {
  protected static final Logger logger = Logger.getLogger(Components.class.getName());

  // TODO replace with DI eg Guice
  private static Components defaultInstance = new Components();

  private final Map<String, ComponentType<?>> components = new TreeMap<>();
  private final Map<DataType, ComponentType<?>> defaultComponents = new HashMap<>();
  private final WeakHashMap<Widget, Widget> activeWidgets = new WeakHashMap<>();

  /**
   * Gets the default widget registry.
   */
  public static Components getDefault() {
    return defaultInstance;
  }

  /**
   * Sets the default instance to use. <strong>This may only be called from tests</strong>.
   *
   * @throws IllegalStateException if not called from a test
   */
  @VisibleForTesting
  public static void setDefault(Components instance) {
    TestUtils.assertRunningFromTest();
    defaultInstance = instance;
  }

  @Override
  public void register(ComponentType type) {
    if (components.containsKey(type.getName())) {
      throw new IllegalArgumentException("Component class " + type.getClass().getName() + " is already registered");
    }
    components.put(type.getName(), type);
    addItem(type);
  }

  /**
   * Convenience overload for registering annotated widgets.
   */
  public <T extends Widget> void register(Class<T> widgetClass) {
    validateAnnotatedComponentClass(widgetClass);
    Description description = widgetClass.getAnnotation(Description.class);

    WidgetType widgetType = new AbstractWidgetType(description) {
      @Override
      public Widget get() {
        return viewFor(widgetClass).orElseGet(() -> {
          try {
            return widgetClass.newInstance();
          } catch (InstantiationException | IllegalAccessException e) {
            logger.log(Level.WARNING, "error creating widget", e);
            return null;
          }
        });
      }
    };

    register(widgetType);
  }

  /**
   * Validates a component class. An exception is thrown if the class does not have a {@link Description @Description}
   * annotation, or if its name is empty.
   *
   * @throws InvalidWidgetException if the component class is not valid
   */
  public static <T extends Component> void validateAnnotatedComponentClass(Class<T> componentClass) {
    requireNonNull(componentClass, "componentClass");

    if (!componentClass.isAnnotationPresent(Description.class)) {
      throw new InvalidWidgetException(
          "No description present on component class " + componentClass.getName());
    }
    Description description = componentClass.getAnnotation(Description.class);

    if (description.name().isEmpty()) {
      throw new InvalidWidgetException("No name specified for the widget");
    }
  }

  @Override
  public void unregister(ComponentType type) {
    components.remove(type.getName());
    List<DataType> defaultWidgetsToRemove = defaultComponents.entrySet().stream()
        .filter(e -> e.getValue().getName().equals(type.getName()))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    defaultWidgetsToRemove.forEach(defaultComponents::remove);
    removeItem(type);
  }

  public Stream<ComponentType<?>> allComponents() {
    return components.values().stream();
  }

  public Stream<WidgetType> allWidgets() {
    return allComponents().flatMap(TypeUtils.castStream(WidgetType.class));
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
    Optional<Widget> widget = createWidget(name);
    widget.ifPresent(w -> w.setSource(source));
    return widget;
  }

  /**
   * Tries to create a widget from a known widget name, without an initial source.
   */
  public Optional<Widget> createWidget(String name) {
    Optional<Widget> widget = typeFor(name).map(ComponentType::get).flatMap(TypeUtils.optionalCast(Widget.class));
    widget.ifPresent(w -> activeWidgets.put(w, w));
    return widget;
  }

  /**
   * Tries to create an arbitrary component. Will delegate to createWidget if the given component
   * name is registered as a widget.
   */
  public Optional<? extends Component> createComponent(String name) {
    // Widgets need to be created using the createWidget function due to state
    Optional<Widget> widget = typeFor(name).filter(WidgetType.class::isInstance).flatMap(_w -> createWidget(name));
    if (widget.isPresent()) {
      return widget;
    } else {
      return typeFor(name).map(ComponentType::get);
    }
  }

  public Optional<Type> javaTypeFor(String name) {
    return typeFor(name).map(ComponentType::get).map(Object::getClass);
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
   * @return a ComponentType to create widgets of the same class
   */
  private Optional<ComponentType<?>> typeFor(String name) {
    return Optional.ofNullable(components.get(name));
  }

  private Set<WidgetType> getWidgetsForType(DataType type) {
    return allWidgets()
        .filter(d -> DataTypes.isCompatible(type, d.getDataTypes()))
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
   * Sets the default component to use for a given data type.
   *
   * @param dataType   the type to set the default widget for
   * @param widgetType the type of widget to set as the default
   */
  public void setDefaultComponent(DataType dataType, ComponentType<?> widgetType) {
    defaultComponents.put(dataType, widgetType);
  }

  /**
   * Gets the name of the default widget for the given data type, or {@link Optional#empty()} if there is no default
   * widget for that type.
   */
  public Optional<String> defaultWidgetNameFor(DataType type) {
    return Optional.ofNullable(defaultComponents.get(type)).map(ComponentType::getName);
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

  /**
   * Create an instance for a ParametrizedController annotated class.
   */
  public static <T> Optional<T> viewFor(Class<T> annotatedClass) {
    ParametrizedController controller = annotatedClass.getAnnotation(ParametrizedController.class);

    if (controller != null) { //NOPMD readability
      try {
        FXMLLoader loader = new FXMLLoader(annotatedClass.getResource(controller.value()));
        loader.load();
        return Optional.of(loader.getController());
      } catch (IOException e) {
        throw new RuntimeException("Could not instantiate the FXML controller", e);
      }
    }

    return Optional.empty();
  }
}
