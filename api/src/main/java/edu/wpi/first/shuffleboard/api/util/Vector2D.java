package edu.wpi.first.shuffleboard.api.util;

import java.util.Objects;

/**
 * A vector in 2-dimensional Cartesian space.
 */
public final class Vector2D {

  public final double x;
  public final double y;

  /**
   * Creates a vector from the origin to the point <tt>(x, y)</tt>.
   *
   * @param x the X-coordinate of the vector
   * @param y the Y-coordinate of the vector
   */
  public Vector2D(double x, double y) {
    this.x = x;
    this.y = y;
  }

  /**
   * Gets the X-coordinate of this vector.
   */
  public double getX() {
    return x;
  }

  /**
   * Gets the Y-coordinate of this vector.
   */
  public double getY() {
    return y;
  }

  /**
   * Gets the magnitude of this vector.
   */
  public double getMagnitude() {
    return Math.sqrt(x * x + y * y);
  }

  /**
   * Gets the angle of this vector, in radians in the range <tt>(-pi, pi)</tt>.
   */
  public double getAngle() {
    return Math.atan2(y, x);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Vector2D that = (Vector2D) o;
    return this.x == that.x
        && this.y == that.y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  @Override
  public String toString() {
    return "Vector2D(" + x + "," + y + ")";
  }

}
