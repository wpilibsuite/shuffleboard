package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.util.Map;
import java.util.Objects;

public class UltrasonicData extends ComplexData<UltrasonicData> {

  private final double rangeInches;

  public UltrasonicData(double rangeInches) {
    this.rangeInches = rangeInches;
  }

  public double getRangeInches() {
    return rangeInches;
  }

  @Override
  public Map<String, Object> asMap() {
    return Map.of("Value", rangeInches);
  }

  @Override
  public String toHumanReadableString() {
    return String.format("%.3f Inches", rangeInches);
  }

  @Override
  public String toString() {
    return "UltrasonicData(range=" + rangeInches + " inches)";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UltrasonicData that = (UltrasonicData) o;
    return Double.compare(that.rangeInches, rangeInches) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(rangeInches);
  }
}
