package edu.wpi.first.shuffleboard.api.util;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Function;

/**
 * Utility class for maps.
 */
public final class Maps {

  private Maps() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Creates a new map builder.
   *
   * @param <K> the type of the keys in the final map
   * @param <V> the type of the values in the final map
   *
   * @return a new empty builder for a map
   */
  public static <K, V> MapBuilder<K, V> builder() {
    return new MapBuilder<>();
  }

  /**
   * An unsafe version of {@link Map#computeIfAbsent(Object, Function) Map.computeIfAbsent} that may throw a checked
   * exception.
   *
   * @param map      the map to modify
   * @param key      the key to get
   * @param function the compute function
   * @param <K>      the type of keys in the map
   * @param <V>      the type of values in the map
   * @param <X>      the type of exception that the compute function may throw
   *
   * @return
   *
   * @throws X if the compute function threw an exception
   */
  public static <K, V, X extends Throwable> V computeIfAbsent(
      Map<K, V> map, K key, ThrowingFunction<? super K, ? extends V, ? extends X> function) throws X {
    Throwable[] exception = {null};
    final Object none = new Object();
    V value = map.computeIfAbsent(key, k -> {
      try {
        return function.apply(k);
      } catch (Throwable e) {
        exception[0] = e;
        return (V) none;
      }
    });
    if (value == none) { // NOPMD reference equality check
      throw (X) exception[0];
    } else {
      return value;
    }
  }

  /**
   * Gets the element mapped to {@code K} in a map, casting it as needed.
   *
   * @param map the map to get an element from
   * @param key the key the element should be mapped to
   * @param <K> the type of keys in the map
   * @param <T> the type of the value to return
   *
   * @throws NoSuchElementException if the key is not in the map
   * @throws ClassCastException     if the value is present, but is not an instance of {@code T}
   */
  public static <K, T> T get(Map<? super K, ?> map, K key) throws NoSuchElementException {
    if (map.containsKey(key)) {
      return (T) map.get(key);
    } else {
      throw new NoSuchElementException("No such key: " + key);
    }
  }

  /**
   * Gets the element mapped to {@code key} in a map, casting it as needed.
   *
   * @param map          the map to get an element from
   * @param key          the key the element should be mapped to
   * @param defaultValue the default value to use if the map does not contain the given key
   * @param <K>          the type of keys in the map
   * @param <V>          the type of values in the map
   * @param <T>          the expected type of the element
   *
   * @return the
   *
   * @throws ClassCastException if the element is present in the map, but is not an instance of {@code T}
   */
  public static <K, V, T extends V> T getOrDefault(Map<? super K, V> map, K key, T defaultValue) {
    if (map.containsKey(key)) {
      return (T) map.get(key);
    } else {
      return defaultValue;
    }
  }

  /**
   * A builder class for maps.
   *
   * @param <K> the type of the keys in the map to build
   * @param <V> the type of the values in the map to build
   */
  public static final class MapBuilder<K, V> {

    private boolean completed = false;
    private final Map<K, V> built = new HashMap<>();

    /**
     * Puts the given key-value entry into the map and returns this builder to allow for method chaining.
     *
     * @param key   the entry's key
     * @param value the entry's value
     *
     * @return this builder
     */
    public MapBuilder<K, V> put(K key, V value) {
      if (completed) {
        throw new IllegalStateException("Cannot modify a builder after it has built");
      }
      built.put(key, value);
      return this;
    }

    /**
     * Puts all the contents of a map into the builder.
     *
     * @param map the map whose contents to add
     *
     * @return this builder
     */
    public MapBuilder<K, V> putAll(Map<? extends K, ? extends V> map) {
      if (completed) {
        throw new IllegalStateException("Cannot modify a builder after it has built");
      }
      built.putAll(map);
      return this;
    }

    /**
     * Returns the built map. The builder is no longer modifiable after calling this method.
     */
    public Map<K, V> build() {
      completed = true;
      return built;
    }

  }

}
