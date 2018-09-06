package edu.wpi.first.shuffleboard.api.sources;

import com.google.common.collect.ImmutableSet;

import edu.wpi.first.shuffleboard.api.data.DataType;

import java.util.Optional;
import java.util.Set;

@SuppressWarnings("PMD.UseUtilityClass")
public class DummySource<T> extends AbstractDataSource<T> {
  /**
   * Create a new static, unchanging source for the given data type and value.
   */
  public DummySource(DataType<T> dataType, T value) {
    super(dataType);
    this.setActive(true);
    this.setName(dataType.getName());
    this.setData(value);
  }

  @Override
  public SourceType getType() {
    return SourceTypes.Static;
  }

  public static <T> DummySource<T> forType(DataType<T> type) {
    return new DummySource<>(type, type.getDefaultValue());
  }

  /**
   * Return an example source value for the given data types.
   * If no example source value could be found, then None is returned instead.
   */
  @SuppressWarnings("unchecked")
  public static Optional<DataSource<?>> forTypes(Set<DataType> types) {
    if (types.isEmpty()) {
      return Optional.empty();
    }
    if (types.stream().anyMatch(DataType::isComplex)) {
      DataType type = types.stream().filter(DataType::isComplex).findFirst().get();
      return Optional.of(new DummySource(type, type.getDefaultValue()));
    } else {
      DataType type = (DataType) types.toArray()[0];
      return Optional.of(new DummySource(type, type.getDefaultValue()));
    }
  }

  public static Optional<DataSource<?>> forTypes(DataType... types) {
    return forTypes(ImmutableSet.copyOf(types));
  }

}
