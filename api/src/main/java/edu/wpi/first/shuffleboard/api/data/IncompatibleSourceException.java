package edu.wpi.first.shuffleboard.api.data;

import java.util.Set;
import java.util.stream.Collectors;

public class IncompatibleSourceException extends RuntimeException {
  private final Set<DataType> expected;
  private final DataType found;

  /**
   * Represents binding sources of incompatible types to a widget.
   *
   * @param expected the source types that were expected
   * @param found the source types that were passed
   */
  public IncompatibleSourceException(Set<DataType> expected, DataType found) {
    super(String.format("Expected one of (%s), but found type %s instead",
            String.join(",",
                    expected.stream().map(DataType::toString).collect(Collectors.toList())),
            found));
    this.expected = expected;
    this.found = found;
  }

  public Set<DataType> getTypesExpected() {
    return expected;
  }

  public DataType getTypesFound() {
    return found;
  }
}
