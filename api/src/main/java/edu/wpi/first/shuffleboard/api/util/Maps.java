package edu.wpi.first.shuffleboard.api.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for maps.
 */
public final class Maps {

  private Maps() {
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
     * Returns the built map. The builder is no longer modifiable after calling this method.
     */
    public Map<K, V> build() {
      completed = true;
      return built;
    }

  }

}
