package edu.wpi.first.shuffleboard.api.data;

import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;
import edu.wpi.first.shuffleboard.api.util.EqualityUtils;
import edu.wpi.first.shuffleboard.api.util.Maps;
import edu.wpi.first.shuffleboard.api.util.StringUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A complex data type backed internally by an observable map. Subtypes should have properties
 * bound to specific keys in the map.
 *
 * @param <T> the self type
 */
public abstract class ComplexData<T extends ComplexData<T>> {

  /**
   * Creates a map containing all the individual data points composing this complex data object. For example, a data
   * class looking like:
   * <pre>{@code
   * public class Point extends ComplexData<Point> {
   *   private final double x;
   *   private final double y;
   * }}</pre>
   * should put the values of {@code x} and {@code y} into this map, with the keys {@code "x"} and {@code "y"},
   * respectively. Note that these keys do not have to be the same as the field names, and may be completely arbitrary.
   * But the keys <i>do</i> have to be the same as those used by the corresponding {@link ComplexDataType} to convert
   * a {@code Map<String, Object>} to an instance of this class.
   */
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
    Maps.MapBuilder<String, Object> builder = Maps.builder();
    Map<String, Object> otherMap = other.asMap();
    thisMap.forEach((key, value) -> {
      if (EqualityUtils.isDifferent(value, otherMap.get(key))) {
        builder.put(key, value);
      }
    });
    return builder.build();
  }

  /**
   * Generates a human-readable string representing this data. The default implementation simply maps each key-value
   * pair from {@link #asMap()} to the format {@code "key=value"} with a comma ({@code ','}) delimiting each pair.
   * Pairs are sorted alphanumerically by key. Subclasses are free to override this method if a different order is
   * desired.
   */
  public String toHumanReadableString() {
    var map = asMap();
    return map.entrySet()
        .stream()
        .sorted(Comparator.comparing(Map.Entry::getKey, AlphanumComparator.INSTANCE))
        .map(e -> StringUtils.deepToString(e.getKey()) + "=" + StringUtils.deepToString(e.getValue()))
        .collect(Collectors.joining(", "));
  }

}
