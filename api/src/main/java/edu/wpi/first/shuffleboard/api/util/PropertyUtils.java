package edu.wpi.first.shuffleboard.api.util;

import org.fxmisc.easybind.EasyBind;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import javafx.beans.binding.Binding;
import javafx.beans.property.Property;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

/**
 * Utility methods for working with JavaFX properties.
 */
public final class PropertyUtils {

  // used to keep track of bindings so they don't get GC'd too early
  private static final Map<Property, Binding> bindings = new WeakHashMap<>();

  private PropertyUtils() {
  }

  /**
   * Binds two properties bidirectionally. This is more powerful than the methods in
   * {@link javafx.beans.binding.Bindings} because the two properties can be of any type, not just
   * the same type (eg {@code firstProperty} can be {@code String} and {@code secondProperty} can be
   * {@code Double} and the bindings will "just work").
   *
   * @param firstProperty  the first property to bind
   * @param secondProperty the second property to bind
   * @param t2uConverter   the conversion function to convert values of type {@code T} to {@code U}
   * @param u2tConverter   the conversion function to convert values of type {@code U} to {@code T}
   */
  public static <T, U> void bindBidirectionalWithConverter(
      Property<T> firstProperty,
      Property<U> secondProperty,
      Function<T, U> t2uConverter,
      Function<U, T> u2tConverter) {
    firstProperty.setValue(u2tConverter.apply(secondProperty.getValue()));
    firstProperty.addListener((__, old, newValue) ->
        secondProperty.setValue(t2uConverter.apply(newValue)));
    secondProperty.addListener((__, old, newValue) ->
        firstProperty.setValue(u2tConverter.apply(newValue)));
  }

  /**
   * Binds {@code firstProperty} to {@code secondProperty}, using a conversion function to map
   * values of type {@code U} to {@code T} so the first property can be bound.
   *
   * @param firstProperty  the property to bind
   * @param secondProperty the property to bind to
   * @param u2tConverter   the conversion function
   */
  public static <T, U> void bindWithConverter(
      Property<T> firstProperty,
      Property<U> secondProperty,
      Function<U, T> u2tConverter) {
    Binding<T> binding = EasyBind.monadic(secondProperty).map(u2tConverter);
    bindings.put(firstProperty, binding);
    firstProperty.bind(binding);
  }

  /**
   * Binds a property to a specific key in a map. If there is no entry for that key, the property's
   * value will be set to null.
   *
   * @param property the property to bind
   * @param map      the map to bind to
   * @param key      the key for the entry to bind to
   * @param v2t      a conversion function for converting objects of type <i>V</i> to type <i>T</i>
   * @param <K>      the types of the keys in the map
   * @param <V>      the types of the values in the map
   * @param <T>      the type of data in the property
   */
  public static <K, V, T extends V> void bindToMapBidirectionally(Property<T> property,
                                                                  ObservableMap<K, V> map,
                                                                  K key,
                                                                  Function<V, T> v2t) {
    property.addListener((__, oldValue, newValue) -> map.put(key, newValue));
    map.addListener((MapChangeListener<K, V>) change -> {
      if (change.getKey().equals(key)) {
        if (change.wasRemoved() && !map.containsKey(key)) {
          property.setValue(null);
        } else if (change.wasAdded()) {
          property.setValue(v2t.apply(change.getValueAdded()));
        }
      }
    });
  }

  /**
   * Combines multiple observable lists into a single one. The combined list is initially the concatenation of
   * {@code second} to the end {@code first}. Subsequent additions to either list will be appended to the end of
   * the combined list. Removing items from either list will also remove those specific items; in this case,
   * reference equality is used instead of object equality to ensure that only the exact objects that were removed from
   * the original list are removed from the combined one.
   *
   * @param first  the first list
   * @param second the second list
   * @param <T>    the type of data in the lists
   */
  public static <T> ObservableList<T> combineLists(ObservableList<T> first, ObservableList<T> second) {
    ObservableList<T> combine = FXCollections.observableArrayList();
    ListChangeListener<? super T> listener = c -> {
      while (c.next()) {
        if (c.wasAdded()) {
          combine.addAll(c.getAddedSubList());
        } else if (c.wasRemoved()) {
          // Only remove the actual elements that were removed from the list
          // Otherwise all instances of an element would be removed, which is not the desired behavior
          c.getRemoved().forEach(e -> combine.removeIf(t -> e == t));
        }
      }
    };
    combine.addAll(first);
    combine.addAll(second);
    first.addListener(listener);
    second.addListener(listener);
    return combine;
  }

}
