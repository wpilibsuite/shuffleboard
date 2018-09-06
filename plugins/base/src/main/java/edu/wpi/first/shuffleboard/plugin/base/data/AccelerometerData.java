package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.util.Maps;

import java.util.Map;
import java.util.Objects;

public final class AccelerometerData extends ComplexData<AccelerometerData> {

  private final double value;

  public AccelerometerData(double value) {
    this.value = value;
  }

  public double getValue() {
    return value;
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.<String, Object>builder()
        .put("Value", value)
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccelerometerData that = (AccelerometerData) o;
    return this.value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return String.format("AccelerometerData(value=%s)", value);
  }

}
