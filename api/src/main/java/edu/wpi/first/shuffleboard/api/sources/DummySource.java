package edu.wpi.first.shuffleboard.api.sources;

import com.google.common.collect.ImmutableSet;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;

import java.util.Set;

@SuppressWarnings("PMD.UseUtilityClass")
public class DummySource<T> extends AbstractDataSource<T> {
  /**
   * Create a new static, unchanging source for the given data type and value.
   */
  public DummySource(DataType dataType, T value) {
    super(dataType);
    this.setActive(true);
    this.setName(dataType.getName());
    this.setData(value);
  }

  @Override
  public SourceType getType() {
    return SourceTypes.Static;
  }

  /**
   * Return an example source value for the given data types.
   * If no example source value could be found, then None is returned instead.
   */
  @SuppressWarnings("unchecked")
  public static DataSource<?> forTypes(Set<DataType> types) {
    if (types.isEmpty()) {
      return DataSource.none();
    }

    if (types.stream().anyMatch(DataType::isComplex)) {
      DataType type = types.stream().filter(DataType::isComplex).findFirst().get();
      return new DummySource(type, type.getDefaultValue());
    } else {
      DataType type = (DataType) types.toArray()[0];
      return new DummySource(type, type.getDefaultValue());
    }
  }

  public static DataSource<?> forTypes(DataType... types) {
    return forTypes(ImmutableSet.copyOf(types));
  }

  /**
   * Return a dummy source for the name of the given DataType.
   * Intended to be used by {@link SourceTypes#forUri}
   */
  public static DataSource forTypeName(String name) {
    return DummySource.forTypes(DataTypes.getDefault().forName(name)
        .orElseThrow(() -> new RuntimeException("No DataType " + name)));
  }
}
