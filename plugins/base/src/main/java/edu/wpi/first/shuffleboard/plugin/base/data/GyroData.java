package edu.wpi.first.shuffleboard.plugin.base.data;

import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.util.Map;
import java.util.Objects;

public final class GyroData extends ComplexData<GyroData> {

  private final double value;

  public GyroData(double value) {
    this.value = value;
  }

  public GyroData(Map<String, Object> map) {
    this((Double) map.getOrDefault("Value", 0.0));
  }

  @Override
  public Map<String, Object> asMap() {
    return ImmutableMap.of("Value", value);
  }
  
  /**
   * Version of getValue that always returns value between 0 and 360.
   * This is guaranteed to be displayed properly by the Gyro widget. 
   */
  public double getWrappedValue() {
    if (value < 0) {
      return ((value % 360) + 360) % 360;
    } else {
      return value % 360;
    }
  }

  public double getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("GyroData(value=%s)", value);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    GyroData that = (GyroData) obj;
    return this.value == that.value;
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toHumanReadableString() {
    return value + " Degrees";
  }
}
