package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.util.CollectionUtils;
import javafx.collections.ObservableMap;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A widget is a UI element that displays data from a {@link DataSource} and has the ability to modify
 * that data. Simple widgets can display singular data points (eg some text, a number, a boolean value, etc);
 * composite widgets display complex data like subsystems or sensors.
 *
 * <p> Generally, a widget is defined with {@link #simpleWidget(Consumer) simpleWidget} and
 * {@link #compositeWidget(Consumer) compositeWidget} for simple and composite widgets, respectively. These
 * static methods take care of registering the widget with the {@link Widgets} utility class, which also handles
 * verification. This style of creating and defining widgets is designed for use in DSLs with Kotlin or Groovy that
 * allow for fluent method nesting to concisely define widgets.
 *
 * <p> Widgets have a few important properties:
 * <ul>
 * <li>Name</li>
 * <li>A preferred size</li>
 * <li>Collection of views for display at different sizes</li>
 * <li>Collection of supported data types</li>
 * <li>A data source</li>
 * </ul>
 *
 * <p> The name of a widget should be unique much like a class or package name. It's a human-readable identifier that
 * allows users to easily understand what widgets are being displayed, and which widgets can display which data
 * types.
 *
 * <p> A widget has UI elements (technically, <i>suppliers</i> of UI elements) that are used to display the widget
 * at varying sizes. A widget cannot be viewed any smaller than the size of the smallest view. If a preferred size
 * is given, a newly created widget will be displayed at that size with the appropriate UI view.
 *
 * <p> All widgets can display some arbitrary amount and combination of {@link DataType data types}. By specifying
 * these data types, the widget declares that it can <i>always</i> handle that kind of data. For example, a
 * "Text Display" widget could hypothetically support Text, Number, and Boolean. This means that any data of
 * type Text, Number, or Boolean could be displayed with a "Text Display" widget.
 *
 * <p> The data source provides an easy way for a widget to get information about the data it handles. More information
 * about data sources can be found {@link DataSource here}.
 *
 * @param <T> the type of data the widget supports. For composite widgets, this is always
 *            {@link ObservableMap ObservableMap&lt;String, Object&gt;}.
 */
public class Widget<T> {

  protected String name = "";
  protected Size preferredSize = null;
  protected final Map<Size, Supplier<Pane>> views = new TreeMap<>();
  protected final Set<DataType> dataTypes = new HashSet<>();
  protected DataSource<T> source = DataSource.none();

  /**
   * An empty default constructor that does no configuration. Newly created widgets must have their properties
   * initialized with {@link #configure(Consumer)} (for fluent DSLs) or by setting the fields manually (for subclasses)
   */
  public Widget() {

  }

  /**
   * Configures this widget with the given configuration function. The function should specify all the properties of
   * the widget: set the name, specify which data types it supports, and define how it should be viewed at
   * different sizes.
   */
  // TODO this should probably be part of a configurable subclass
  public void configure(Consumer<Widget<T>> configuration) {
    configuration.accept(this);
  }

  /**
   * Adds a view able to display this widget when it is set to a certain size in the UI.
   *
   * @param size          the size of the given view
   * @param viewSupplier  a supplier for root view pane
   * @param configuration a function used to populate the view pane with UI elements and controls
   * @param <P>           the type of the root pane
   */
  public <P extends Pane> void addView(Size size,
                                       Supplier<? extends P> viewSupplier,
                                       Consumer<? super P> configuration) {
    views.put(size, () -> {
      P pane = viewSupplier.get();
      configuration.accept(pane);
      return pane;
    });
  }

  /**
   * Adds a view that is displayed inside a {@link StackPane}. This is equivalent to
   * {@code addView(size, StackPane::new, configuration)}
   *
   * @param size          the minimum size of the view
   * @param configuration a function used to populate the view pane with UI elements and controls
   */
  public void addView(Size size, Consumer<? super StackPane> configuration) {
    addView(size, StackPane::new, configuration);
  }

  /**
   * Creates a simple (single data point) widget with the given configuration function. This function should set up the
   * widget as laid out {@link #configure(Consumer) here}.
   *
   * @param configure a function used to define and configure the widget
   * @param <T>       the type (or super type if supporting multiple disparate types like Text and Number) of data the
   *                  widget handles
   */
  public static <T> void simpleWidget(Consumer<Widget<T>> configure) {
    try {
      Widgets.register(configure);
    } catch (InvalidWidgetException ex) {
      // TODO log
      ex.printStackTrace();
    }
  }

  /**
   * Creates a composite widget with the given configuration function. This function should set up the widget as laid
   * out {@link #configure(Consumer) here}.
   *
   * @param configure a function used to define and configure the widget
   */
  public static void compositeWidget(Consumer<Widget<ObservableMap<String, Object>>> configure) {
    simpleWidget(configure);
  }

  // Getters and setters

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name == null || name.isEmpty()) {
      throw new IllegalArgumentException("A widget must have a name");
    }
    this.name = name;
  }

  /**
   * Gets the preferred size of the widget. If none was explicitly set, this will return the size of the smallest
   * view defined in {@link #views}.
   */
  public Size getPreferredSize() {
    return preferredSize != null ? preferredSize : CollectionUtils.elementAt(views.keySet(), 0);
  }

  public void setPreferredSize(Size preferredSize) {
    this.preferredSize = preferredSize;
  }

  /**
   * Gets an unmodifiable copy of this widgets defined views. To add a new view, use {@link #addView(Size, Consumer)} or
   * {@link #addView(Size, Supplier, Consumer)}.
   */
  public Map<Size, Supplier<Pane>> getViews() {
    return Collections.unmodifiableMap(views);
  }

  /**
   * Gets an unmodifiable copy of this widgets supported data types. To specify support for data types, use
   * {@link #supportDataTypes(DataType...)}.
   */
  public Set<DataType> getDataTypes() {
    return Collections.unmodifiableSet(dataTypes);
  }

  public void supportDataTypes(DataType... types) {
    this.dataTypes.addAll(Arrays.asList(types));
  }

  public DataSource<T> getSource() {
    return source;
  }

  public void setSource(DataSource<T> source) {
    this.source = Objects.requireNonNull(source, "A widget must have a source");
  }

}
