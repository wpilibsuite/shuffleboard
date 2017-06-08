package edu.wpi.first.shuffleboard.util;

import org.fxmisc.easybind.EasyBind;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import javafx.beans.binding.Binding;
import javafx.beans.property.Property;

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

}
