package edu.wpi.first.shuffleboard.api.util;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import static java.util.Objects.requireNonNull;

/**
 * Utility methods for JavaFX not available in the standard library.
 */
public final class FxUtils {

  private static final Object FX_CONTROLLER_KEY = new Object();

  private FxUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Runs a task on the JavaFX application thread as soon as possible. If this is called from the
   * application thread, the task will be run <i>immediately</i>. Otherwise, it will be run at
   * some later point.
   *
   * @param task the task to run. If null, the method will return immediately and no action
   *             will be taken.
   *
   * @return a completable future that will have a result of {@code true} once the task has run
   */
  public static CompletableFuture<Boolean> runOnFxThread(Runnable task) {
    requireNonNull(task, "task");

    final CompletableFuture<Boolean> future = new CompletableFuture<>();
    if (Platform.isFxApplicationThread()) {
      task.run();
      future.complete(true);
    } else {
      Platform.runLater(() -> {
        task.run();
        future.complete(true);
      });
    }
    return future;
  }

  /**
   * Binds a property to the value of an entry in a map.
   *
   * @param property  the property to bind
   * @param map       the map to bind to
   * @param key       the key of the entry to bind to
   * @param converter a function for converting map values to a type the property can accept
   * @param <K>       the type of keys in the map
   * @param <V>       the type of values in the map
   * @param <T>       the type of data in the property
   */
  public static <K, V, T> void bind(Property<T> property,
                                    ObservableMap<K, V> map,
                                    K key,
                                    Function<V, T> converter) {
    property.bind(Bindings.createObjectBinding(() -> converter.apply(map.get(key)), map));
  }

  /**
   * Binds a property to the data of a data source.
   *
   * @param property   the property to bind
   * @param dataSource the data source to bind to
   * @param <T>        the type of data of the source
   */
  public static <T> void bind(Property<T> property, DataSource<T> dataSource) {
    property.bind(dataSource.dataProperty());
  }

  /**
   * Binds an observable list to a list property.
   *
   * @param list       the observable list to bind
   * @param observable the property to bind to
   * @param <T>        the type of elements in the list
   */
  public static <T> void bind(ObservableList<? super T> list,
                              ObservableValue<? extends List<? extends T>> observable) {
    list.setAll(observable.getValue());
    observable.addListener((__, oldList, newList) -> list.setAll(newList));
  }

  /**
   * Bidirectionally binds a property and a data source. Changes to one will affect the other.
   *
   * @param property   the property to bind
   * @param dataSource the data source to bind
   * @param <T>        the type of data
   */
  public static <T> void bindBidirectional(Property<T> property, DataSource<T> dataSource) {
    property.bindBidirectional(dataSource.dataProperty());
  }

  /**
   * A more general version of {@link Bindings#when(ObservableBooleanValue)}
   * that can accept general boolean properties as conditions.
   *
   * @param condition the condition to bind to
   *
   * @see Bindings#when(ObservableBooleanValue)
   */
  public static When when(Property<Boolean> condition) {
    if (condition instanceof ObservableBooleanValue) {
      return Bindings.when((ObservableBooleanValue) condition);
    }
    SimpleBooleanProperty realCondition = new SimpleBooleanProperty();
    realCondition.bind(condition);
    return Bindings.when(realCondition);
  }

  /**
   * Converts a JavaFX color to a hex web string in the format {@code #RRGGBBAA}. The string can be read with
   * {@link Color#web(String) Color.web} to create a {@code Color} object that the string represents.
   *
   * @param color the color to convert to a hex string.
   */
  public static String toHexString(Color color) {
    return String.format("#%02X%02X%02X%02X",
        (int) (color.getRed() * 255),
        (int) (color.getGreen() * 255),
        (int) (color.getBlue() * 255),
        (int) (color.getOpacity() * 255));
  }

  /**
   * Creates a menu item with the given text and event handler.
   *
   * @param text         the text of the menu item
   * @param eventHandler the handler to call when the menu item is acted upon
   */
  public static MenuItem menuItem(String text, EventHandler<ActionEvent> eventHandler) {
    MenuItem menuItem = new MenuItem(text);
    menuItem.setOnAction(eventHandler);
    return menuItem;
  }

  /**
   * Creates a disabled menu item for use as a label.
   *
   * @param text the text of the label
   */
  public static MenuItem menuLabel(String text) {
    MenuItem menuItem = new MenuItem(text);
    menuItem.setDisable(true);
    return menuItem;
  }

  /**
   * Gets the label associated with a node. If the node does not have a label, an empty optional is returned.
   *
   * @param node the node to get the label for
   */
  public static Optional<Label> getLabel(Node node) {
    return Optional.ofNullable((Label) node.queryAccessibleAttribute(AccessibleAttribute.LABELED_BY));
  }

  /**
   * Sets the FXML controller for a node.
   *
   * @param node       the node to set the controller for
   * @param controller the FXML controller for the node
   */
  public static void setController(Node node, Object controller) {
    node.getProperties().put(FX_CONTROLLER_KEY, controller);
  }

  /**
   * Gets the FXML controller for a node.
   *
   * @param node the node to get the FXML controller of
   * @param <T>  the type of the controller
   *
   * @throws java.util.NoSuchElementException if no FXML controller has been set for the given node
   * @throws ClassCastException               if the FXML controller has been set to a controller not of type {@code T}
   */
  public static <T> T getController(Node node) {
    return Maps.get(node.getProperties(), FX_CONTROLLER_KEY);
  }

  /**
   * Loads an FXML file specified by its controller class' {@link ParametrizedController} annotation.
   *
   * @param controllerClass the FXML controller class to load
   *
   * @return the root pane of the FXML
   *
   * @throws IllegalArgumentException if the class does not have a {@code ParametrizedController} annotation
   * @throws IOException              if the specified FXML file could not be loaded
   */
  public static <N extends Node> N load(Class<?> controllerClass) throws IllegalArgumentException, IOException {
    ParametrizedController annotation = controllerClass.getAnnotation(ParametrizedController.class);
    if (annotation == null) {
      throw new IllegalArgumentException("FXML controller class has no @ParametrizedController: " + controllerClass);
    }
    return FXMLLoader.load(controllerClass.getResource(annotation.value()));
  }

  /**
   * Fires a close request on a window. This is useful to call on the main application window to allow shutdown
   * listeners to run, since they are not run when <tt>System.exit()</tt> or <tt>Platform.exit()</tt> are called.
   *
   * @param window the window to request to close
   */
  public static void requestClose(Window window) {
    window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
  }

}
