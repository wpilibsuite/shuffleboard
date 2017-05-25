package edu.wpi.first.shuffleboard.util;

import edu.wpi.first.shuffleboard.sources.DataSource;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.When;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.collections.ObservableMap;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Utility methods for JavaFX not available in the standard library.
 */
public final class FxUtils {

  private FxUtils() {
    // no FXUtils instance for you
  }

  /**
   * Runs a task on the JavaFX application thread as soon as possible. If this is called from the
   * application thread, the task will be run <i>immediately</i>. Otherwise, it will be run at
   * some later point.
   *
   * @param task the task to run. If null, the method will return immediately and no action
   *             will be taken.
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

}
