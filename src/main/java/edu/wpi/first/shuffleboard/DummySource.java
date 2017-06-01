package edu.wpi.first.shuffleboard;

import edu.wpi.first.shuffleboard.sources.AbstractDataSource;
import edu.wpi.first.shuffleboard.widget.DataType;

import java.util.Optional;
import java.util.Set;

public class DummySource<T> extends AbstractDataSource<T> {
    private DummySource(DataType dataType, T value) {
      super(dataType);
      this.setActive(true);
      this.setName("example");
      this.setData(value);
    }

    // unfortunately, type erasure.
    @SuppressWarnings("unchecked")
    public static Optional<DummySource> forTypes(Set<DataType> types) {
      if (types.contains(DataType.Number) || types.contains(DataType.All)) {
        return Optional.of(new DummySource(DataType.Number, 123));
      } else {
        return Optional.empty();
      }
    }
}
