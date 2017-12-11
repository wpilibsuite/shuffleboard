package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.util.Maps;

import com.google.common.annotations.VisibleForTesting;
import com.sun.javafx.geom.Vec2d;

import java.util.Map;
import java.util.Objects;

/**
 * Represents data from a mecanum drive base. All motor speeds are in the range <tt>(-1, 1)</tt>.
 */
public final class MecanumDriveData extends ComplexData<MecanumDriveData> {

  private static final String frontLeftMotorSpeed = "Front Left Motor Speed";
  private static final String frontRightMotorSpeed = "Front Right Motor Speed";
  private static final String rearLeftMotorSpeed = "Rear Left Motor Speed";
  private static final String rearRightMotorSpeed = "Rear Right Motor Speed";

  private final double frontLeftSpeed;
  private final double frontRightSpeed;
  private final double rearLeftSpeed;
  private final double rearRightSpeed;
  private final double moment;
  private final Vec2d direction;
  private final double turn;

  /**
   * Creates a new mecanum drive data object. The turning moment and motion vector are derived from the four
   * speeds.
   *
   * @param frontLeftSpeed  the speed of the front-left motor
   * @param frontRightSpeed the speed of the front-right motor
   * @param rearLeftSpeed   the speed of the rear-left motor
   * @param rearRightSpeed  the speed of the rear-right motor
   */
  public MecanumDriveData(double frontLeftSpeed, double frontRightSpeed, double rearLeftSpeed, double rearRightSpeed) {
    this.frontLeftSpeed = frontLeftSpeed;
    this.frontRightSpeed = frontRightSpeed;
    this.rearLeftSpeed = rearLeftSpeed;
    this.rearRightSpeed = rearRightSpeed;
    this.moment = calculateMoment(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
    this.direction = calculateDirectionVector(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
    this.turn = Math.sqrt((direction.x * direction.x) + (direction.y * direction.y)) / moment;
  }

  /**
   * Creates a new mecanum drive data object from a map.
   *
   * @param map the map to create a data object from
   *
   * @throws java.util.NoSuchElementException if the map is missing any entry for any motor speed value
   */
  public static MecanumDriveData fromMap(Map<String, ?> map) {
    return new MecanumDriveData(
        Maps.get(map, frontLeftMotorSpeed),
        Maps.get(map, frontRightMotorSpeed),
        Maps.get(map, rearLeftMotorSpeed),
        Maps.get(map, rearRightMotorSpeed)
    );
  }

  @Override
  public Map<String, Object> asMap() {
    // Only include the wheel speeds, not the derived values
    return Maps.<String, Object>builder()
        .put(frontLeftMotorSpeed, frontLeftSpeed)
        .put(frontRightMotorSpeed, frontRightSpeed)
        .put(rearLeftMotorSpeed, rearLeftSpeed)
        .put(rearRightMotorSpeed, rearRightSpeed)
        .build();
  }

  public double getFrontLeftSpeed() {
    return frontLeftSpeed;
  }

  public double getFrontRightSpeed() {
    return frontRightSpeed;
  }

  public double getRearLeftSpeed() {
    return rearLeftSpeed;
  }

  public double getRearRightSpeed() {
    return rearRightSpeed;
  }

  /**
   * Gets the moment about the drive base's center of rotation. This value is derived from the motor speeds and is in
   * the range <tt>(-1, 1)</tt>.
   */
  public double getMoment() {
    return moment;
  }

  /**
   * Gets a vector describing the direction of movement of the drive base. This vector is derived from the motor speeds
   * and has a magnitude in the range <tt>(-1, 1)</tt>.
   */
  public Vec2d getDirection() {
    return direction;
  }

  /**
   * Gets a value describing how the drive base is turning in a differential-drive style. This is in the range
   * <tt>(-Inf, Inf)</tt>, with negative values being turns to the right and positive values being turns to the left.
   * Values of Infinity mean there is no turning moment.
   */
  public double getTurn() {
    return turn;
  }

  public MecanumDriveData withFrontLeftSpeed(double frontLeftSpeed) {
    return new MecanumDriveData(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
  }

  public MecanumDriveData withFrontRightSpeed(double frontRightSpeed) {
    return new MecanumDriveData(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
  }

  public MecanumDriveData withRearLeftSpeed(double rearLeftSpeed) {
    return new MecanumDriveData(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
  }

  public MecanumDriveData withRearRightSpeed(double rearRightSpeed) {
    return new MecanumDriveData(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
  }

  /**
   * Calculates a direction vector for the drive base caused by the mecanum wheel force vectors. This assumes that all
   * four wheels have equal weight distribution. The vector is scaled to have X and Y components in the range
   * <tt>(-1, 1)</tt>; due to the nature of mecanum drives, the magnitude of the vector is also in the range
   * <tt>(-1, 1)</tt>.
   */
  @VisibleForTesting
  static Vec2d calculateDirectionVector(double fl, double fr, double rl, double rr) {
    return new Vec2d(
        (fl - fr - rl + rr) / 4,
        (fl + fr + rl + rr) / 4
    );
  }

  /**
   * Calculates the sum of all moments caused by the mecanum wheel force vectors about the center of rotation. This
   * assumes that all four wheels have equal weight distribution. The moment is scaled to be in the range
   * <tt>(-1, 1)</tt>, with positive values being counter-clockwise rotation and negative values being clockwise
   * rotation.
   */
  @VisibleForTesting
  static double calculateMoment(double fl, double fr, double rl, double rr) {
    // -x(fl) * h/2 - y(fl) * w/2 + x(fr) * h/2 + y(fr) * w/2 - x(rl) * h/2 - y(rl) * w/2 + x(rr) * h/2 + y(rr) * w/2
    // = h/2 * (-x(fl) + x(fr) - x(rl) + x(rr)) + w/2 * (-y(fl) + y(fr) - y(rl) + y(rr))
    // = (h/2 + w/2) * (-x(fl) + x(fr) - x(rl) + x(rr))
    // Drop the constant factor since we don't know the dimensions of the track or the wheel base
    // Scale by √2/4 to get it in the range (-1, 1)
    // And since the x-component is just speed/√2, the √2's cancel out and we're left with:
    return (-fl + fr - rl + rr) / 4;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MecanumDriveData that = (MecanumDriveData) o;
    return this.frontLeftSpeed == that.frontLeftSpeed
        && this.frontRightSpeed == that.frontRightSpeed
        && this.rearLeftSpeed == that.rearLeftSpeed
        && this.rearRightSpeed == that.rearRightSpeed;
  }

  @Override
  public int hashCode() {
    return Objects.hash(frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed);
  }

  @Override
  public String toString() {
    return String.format(
        "MecanumDriveData(frontLeftSpeed=%s, frontRightSpeed=%s, rearLeftSpeed=%s, rearRightSpeed=%s)",
        frontLeftSpeed, frontRightSpeed, rearLeftSpeed, rearRightSpeed
    );
  }

}
