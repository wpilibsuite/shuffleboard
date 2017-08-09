package edu.wpi.first.shuffleboard.api.data;

import java.util.Map;
import java.util.function.Function;

/**
 * Represents complex data such as POJOs or maps.
 *
 * @param <T> the self type
 */
public interface ComplexDataType<T extends ComplexData> extends DataType<T> {

  /**
   * Gets a function used to create a new data object from a map of values to property names.
   */
  Function<Map<String, Object>, T> fromMap();

  /**
   * Creates a new data object from the given map.
   *
   * @param map the mp of values to create the data from
   */
  default T fromMap(Map<String, Object> map) {
    return fromMap().apply(map);
  }

  default boolean isComplex() {
    return true;
  }

}
