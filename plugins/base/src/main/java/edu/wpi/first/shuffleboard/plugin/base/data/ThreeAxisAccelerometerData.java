package edu.wpi.first.shuffleboard.plugin.base.data;

import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.util.Map;
import java.util.Objects;

public final class ThreeAxisAccelerometerData extends ComplexData<ThreeAxisAccelerometerData> {

  private final double x;
  private final double y;
  private final double z;

  /**
   * Creates a new 3-axis accelerometer data object containing the measured acceleration along the 3 3D axes.
   *
   * @param x the x-axis acceleration, in Gs
   * @param y the y-axis acceleration, in Gs
   * @param z the z-axis acceleration, in Gs
   */
  public ThreeAxisAccelerometerData(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Creates a new data object from a map. The map should contain values for "X", "Y", and "Z" as doubles; otherwise,
   * default values of zero are used.
   */
  public ThreeAxisAccelerometerData(Map<String, Object> map) {
    this((double) map.getOrDefault("X", 0.0),
        (double) map.getOrDefault("Y", 0.0),
        (double) map.getOrDefault("Z", 0.0));
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getZ() {
    return z;
  }

  @Override
  public Map<String, Object> asMap() {
    return ImmutableMap.of("X", x, "Y", y, "Z", z);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    ThreeAxisAccelerometerData that = (ThreeAxisAccelerometerData) obj;
    return this.x == that.x
        && this.y == that.y
        && this.z == that.z;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, z);
  }

  @Override
  public String toString() {
    return String.format("ThreeAxisAccelerometerData(x=%s, y=%s, z=%s)", x, y, z);
  }

  @Override
  public String toHumanReadableString() {
    return String.format("x=%.3fg, y=%.3fg, z=%.3fg", x, y, z);
  }
}
