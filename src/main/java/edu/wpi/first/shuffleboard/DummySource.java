package edu.wpi.first.shuffleboard;

import com.google.common.collect.ImmutableSet;

import edu.wpi.first.shuffleboard.data.DataTypes;
import edu.wpi.first.shuffleboard.data.SendableChooserData;
import edu.wpi.first.shuffleboard.sources.AbstractDataSource;
import edu.wpi.first.shuffleboard.data.DataType;
import edu.wpi.first.shuffleboard.util.Maps;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    if (types.contains(DataTypes.Number) || types.contains(DataTypes.All)) {
      return Optional.of(new DummySource(DataTypes.Number, 123));
    } else if (types.contains(DataTypes.String)) {
      return Optional.of(new DummySource(DataTypes.String, "a string"));
    } else if (types.contains(DataTypes.Boolean)) {
      return Optional.of(new DummySource(DataTypes.Boolean, true));
    } else if (types.contains(DataTypes.SendableChooser)) {
      Map<String, Object> map = Maps.<String, Object>builder()
          .put(SendableChooserData.OPTIONS_KEY, new String[]{"A", "B", "C"})
          .put(SendableChooserData.DEFAULT_OPTION_KEY, "A")
          .put(SendableChooserData.SELECTED_OPTION_KEY, "A")
          .build();
      return Optional.of(new DummySource(DataTypes.SendableChooser, new SendableChooserData(map)));
    } else {
      return Optional.empty();
    }
  }

  public static Optional<DummySource> forTypes(DataType... types) {
    return forTypes(ImmutableSet.copyOf(types));
  }

}
