package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.Registry;
import edu.wpi.first.shuffleboard.api.util.TestUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.WeakHashMap;
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
  private final Map<Component, UUID> allActiveComponents = new WeakHashMap<>();

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
    WidgetType<T> widgetType = WidgetType.forAnnotatedWidget(widgetClass);

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
    List<DataType> defaultComponentsToRemove = defaultComponents.entrySet().stream()
        .filter(e -> e.getValue().getName().equals(type.getName()))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());
    defaultComponentsToRemove.forEach(defaultComponents::remove);
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
   * @return an optional containing the created view, or an empty optional if no widget could be created
   */
  public <T> Optional<Widget> createWidget(String name, DataSource<T> source) {
    Optional<Widget> widget = createWidget(name);
    widget.ifPresent(w -> w.addSource(source));
    widget.ifPresent(this::setId);
    return widget;
  }

  /**
   * Tries to create a widget from a known widget with the given name. If successful, the widgets data sources will be
   * set to the given sources. If any of these sources are incompatible, an {@link IncompatibleSourceException} will be
   * thrown.
   *
   * @param name    the name of the widget to create
   * @param sources the data sources for the widget to use
   *
   * @return an optional containing the created widget, or an empty optional if no widget could be created
   *
   * @throws IncompatibleSourceException if the widget for the given name is incompatible with any of the given sources
   */
  public Optional<Widget> createWidget(String name, Collection<DataSource> sources) throws IncompatibleSourceException {
    Optional<Widget> widget = createWidget(name);
    widget.ifPresent(w -> sources.forEach(w::addSource));
    widget.ifPresent(this::setId);
    return widget;
  }

  /**
   * Tries to create a widget from a known widget name, without an initial source.
   */
  public Optional<Widget> createWidget(String name) {
    Optional<Widget> widget = typeFor(name).map(ComponentType::get).flatMap(TypeUtils.optionalCast(Widget.class));
    widget.ifPresent(w -> activeWidgets.put(w, w));
    widget.ifPresent(this::setId);
    return widget;
  }

  /**
   * Tries to create an arbitrary component. Will delegate to createWidget if the given component
   * name is registered as a widget.
   */
  public Optional<? extends Component> createComponent(String name) {
    // Widgets need to be created using the createWidget function due to state
    Optional<Widget> widget = typeFor(name).filter(WidgetType.class::isInstance).flatMap(__ -> createWidget(name));
    widget.ifPresent(this::setId);
    if (widget.isPresent()) {
      return widget;
    } else {
      return typeFor(name)
          .map(ComponentType::get)
          .map(c -> {
            setId(c);
            return c;
          });
    }
  }

  /**
   * Creates a new component with the given name. If the component takes a source (ie implements {@link Sourced}),
   * its source will be set to the one provided.
   *
   * @param name   the name of the component to create
   * @param source the source for the created component to use, if the component accepts one
   *
   * @return an optional containing the created component, or empty if no component with the given name is registered
   */
  public Optional<? extends Component> createComponent(String name, DataSource<?> source) {
    return createComponent(name)
        .map(c -> {
          if (c instanceof Sourced) {
            ((Sourced) c).addSource(source);
          }
          return c;
        });
  }

  /**
   * Gets the UUID of a component.
   *
   * @param component the component to get the UUID of
   *
   * @return the UUID for the given component
   *
   * @throws IllegalArgumentException if the component does not have a UUID
   */
  public UUID uuidForComponent(Component component) {
    UUID uuid = allActiveComponents.get(component);
    if (uuid == null) {
      throw new IllegalArgumentException("Component is not active: " + component);
    }
    return uuid;
  }

  /**
   * Gets the component with the given UUID.
   *
   * @param uuid the UUID of the component to get
   *
   * @return an optional of the component with the UUID
   */
  public Optional<Component> getByUuid(UUID uuid) {
    return ImmutableMap.copyOf(allActiveComponents)
        .entrySet()
        .stream()
        .filter(e -> e.getValue().equals(uuid))
        .map(Map.Entry::getKey)
        .findFirst();
  }

  /**
   * Sets a unique identifier for a component.
   *
   * @param component the component to set
   */
  private void setId(Component component) {
    allActiveComponents.putIfAbsent(component, UUID.randomUUID());
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

  private Set<ComponentType<?>> getComponentsForType(DataType type) {
    return allComponents()
        .filter(d -> DataTypes.isCompatible(type, d.getDataTypes()))
        .collect(Collectors.toSet());
  }

  /**
   * Gets the names of all the possible components that can display the given type, sorted alphabetically. A component
   * can be created for these with {@link #createWidget(String, DataSource) createWidget}.
   *
   * @param type the type of data to get possible widgets for.
   *
   * @return an alphabetically sorted list containing the names of all known widgets that can display data of the
   *         given type
   */
  public List<String> componentNamesForType(DataType type) {
    return getComponentsForType(type)
        .stream()
        .map(ComponentType::getName)
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
   * Gets the name of the default component for the given data type, or {@link Optional#empty()} if there is no default
   * component for that type.
   */
  public Optional<String> defaultComponentNameFor(DataType type) {
    return Optional.ofNullable(defaultComponents.get(type)).map(ComponentType::getName);
  }

  /**
   * Gets the name of a component that can handle data of the given type. If a default component has been set for that
   * type, the name of the default component is returned; otherwise, the name of the first component returned by
   * {@link #componentNamesForType(DataType)} is used.
   */
  public Optional<String> pickComponentNameFor(DataType type) {
    Optional<String> defaultName = defaultComponentNameFor(type);
    if (defaultName.isPresent()) {
      return defaultName;
    } else {
      List<String> names = componentNamesForType(type);
      if (names.isEmpty()) {
        return Optional.empty();
      } else {
        return Optional.of(names.get(0));
      }
    }
  }

  /**
   * Gets the names of all the possible components than can display the data in a given source.
   */
  public List<String> componentNamesForSource(DataSource<?> source) {
    return componentNamesForType(source.getDataType());
  }

  /**
   * Create an instance for a ParametrizedController annotated class.
   */
  public static <T> Optional<T> viewFor(Class<T> annotatedClass) {
    ParametrizedController controller = annotatedClass.getAnnotation(ParametrizedController.class);

    if (controller != null) { //NOPMD readability
      try {
        FXMLLoader loader = new FXMLLoader(annotatedClass.getResource(controller.value()));
        loader.setClassLoader(annotatedClass.getClassLoader());
        loader.load();
        return Optional.of(loader.getController());
      } catch (IOException e) {
        throw new RuntimeException("Could not instantiate the FXML controller", e);
      }
    }

    return Optional.empty();
  }
}
