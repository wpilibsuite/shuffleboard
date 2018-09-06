package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.util.Maps;

import java.util.Map;
import java.util.Objects;

/**
 * Represents data from a differential drive base. All motor speeds are in the range <tt>(-1, 1)</tt>.
 */
public final class DifferentialDriveData extends DriveBaseData<DifferentialDriveData> {

  private final double leftSpeed;
  private final double rightSpeed;

  /**
   * Creates a new differential drive data object.
   *
   * @param leftSpeed    the speed of the left motor
   * @param rightSpeed   the speed of the right motor
   * @param controllable if the drive base is use-controllable
   */
  public DifferentialDriveData(double leftSpeed, double rightSpeed, boolean controllable) {
    super(controllable);
    this.leftSpeed = leftSpeed;
    this.rightSpeed = rightSpeed;
  }

  /**
   * Creates a new differential drive data object from a map.
   *
   * @param map the map to create a data object from
   *
   * @throws java.util.NoSuchElementException if the map is missing any motor speed entry
   */
  public static DifferentialDriveData fromMap(Map<String, Object> map) {
    return new DifferentialDriveData(
        Maps.getOrDefault(map, "Left Motor Speed", 0.0),
        Maps.getOrDefault(map, "Right Motor Speed", 0.0),
        Maps.getOrDefault(map, ".controllable", false)
    );
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.<String, Object>builder()
        .put("Left Motor Speed", leftSpeed)
        .put("Right Motor Speed", rightSpeed)
        .put(".controllable", isControllable())
        .build();
  }

  public double getLeftSpeed() {
    return leftSpeed;
  }

  public double getRightSpeed() {
    return rightSpeed;
  }

  public DifferentialDriveData withLeftSpeed(double leftSpeed) {
    return new DifferentialDriveData(leftSpeed, rightSpeed, isControllable());
  }

  public DifferentialDriveData withRightSpeed(double rightSpeed) {
    return new DifferentialDriveData(leftSpeed, rightSpeed, isControllable());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DifferentialDriveData that = (DifferentialDriveData) o;
    return this.leftSpeed == that.leftSpeed
        && this.rightSpeed == that.rightSpeed
        && this.isControllable() == that.isControllable();
  }

  @Override
  public int hashCode() {
    return Objects.hash(leftSpeed, rightSpeed, isControllable());
  }

  @Override
  public String toString() {
    return String.format("DifferentialDriveData(leftSpeed=%s, rightSpeed=%s, controllable=%s)",
        leftSpeed, rightSpeed, isControllable());
  }

}
