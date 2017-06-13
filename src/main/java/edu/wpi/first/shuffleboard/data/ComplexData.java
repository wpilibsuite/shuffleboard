package edu.wpi.first.shuffleboard.data;

import edu.wpi.first.shuffleboard.util.EqualityUtils;
import edu.wpi.first.shuffleboard.util.Maps;

import java.util.Map;

/**
 * A complex data type backed internally by an observable map. Subtypes should have properties
 * bound to specific keys in the map.
 *
 * @param <T> the self type
 */
public abstract class ComplexData<T extends ComplexData<T>> {

  public abstract Map<String, Object> asMap();

  /**
   * Gets a map of changes that, when applied to {@code other}, would result in data identical
   * to this one.
   *
   * @param other the complex data to base the changes off of
   */
  public final Map<String, Object> changesFrom(T other) {
    Map<String, Object> thisMap = asMap();
    if (other == null) {
      return thisMap;
    }
    Maps.Builder<String, Object> builder = Maps.builder();
    Map<String, Object> otherMap = other.asMap();
    thisMap.forEach((key, value) -> {
      if (EqualityUtils.isDifferent(value, otherMap.get(key))) {
        builder.put(key, value);
      }
    });
    return builder.build();
  }

}
