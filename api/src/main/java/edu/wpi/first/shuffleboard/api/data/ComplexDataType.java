package edu.wpi.first.shuffleboard.api.data;

import java.util.Map;
import java.util.function.Function;

/**
 * Represents complex data such as POJOs or maps.
 *
 * @param <T> the self type
 */
public abstract class ComplexDataType<T extends ComplexData> extends DataType<T> {

  protected ComplexDataType(String name, Class<T> javaClass) {
    super(name, javaClass);
  }

  /**
   * Gets a function used to create a new data object from a map of values to property names.
   */
  public abstract Function<Map<String, Object>, T> fromMap();

  /**
   * Creates a new data object from the given map.
   *
   * @param map the map of values to create the data from
   *
   * @throws IncompleteDataException if the map does not have all the variables needed to create a data object
   */
  public T fromMap(Map<String, Object> map) throws IncompleteDataException {
    try {
      return fromMap().apply(map);
    } catch (RuntimeException e) {
      throw new IncompleteDataException("Incomplete data in map: " + map, e);
    }
  }

  @Override
  public final boolean isComplex() {
    return true;
  }

}
