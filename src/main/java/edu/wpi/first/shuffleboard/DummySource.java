package edu.wpi.first.shuffleboard;

import com.google.common.collect.ImmutableSet;

import edu.wpi.first.shuffleboard.sources.AbstractDataSource;
import edu.wpi.first.shuffleboard.widget.DataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javafx.collections.FXCollections;

@SuppressWarnings("PMD.UseUtilityClass")
public class DummySource<T> extends AbstractDataSource<T> {
  /**
   * Create a new static, unchanging source for the given data type and value.
   */
  public DummySource(DataType dataType, T value) {
    super(dataType);
    this.setActive(true);
    this.setName("example");
    this.setData(value);
  }

  /**
   * Return an example source value for the given data types.
   * If no example source value could be found, then None is returned instead.
   */
  @SuppressWarnings("unchecked")
  public static Optional<DummySource> forTypes(Set<DataType> types) {
    if (types.contains(DataType.Number) || types.contains(DataType.All)) {
      return Optional.of(new DummySource(DataType.Number, 123));
    } else if (types.contains(DataType.String)) {
      return Optional.of(new DummySource(DataType.String, "a string"));
    } else if (types.contains(DataType.Boolean)) {
      return Optional.of(new DummySource(DataType.Boolean, true));
    } else if (types.contains(DataType.SendableChooser)) {
      Map<String, Object> map = new HashMap<>();
      map.put("options", new String[]{"A", "B", "C"});
      map.put("default", "A");
      map.put("selected", null);
      return Optional.of(new DummySource(DataType.SendableChooser,
          FXCollections.observableMap(map)));
    } else {
      return Optional.empty();
    }
  }

  public static Optional<DummySource> forTypes(DataType... types) {
    return forTypes(ImmutableSet.copyOf(types));
  }

}
