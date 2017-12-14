package edu.wpi.first.shuffleboard.api.components;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;

/**
 * A utility class for generating curved arrows. The generated arrows follow a <i>circular</i>, rather than
 * <i>elliptical</i>, path. The heads of the arrows are always the same isosceles triangle with the length of the base
 * equalling the height of the triangle; this works out to be (approximately) a 63-63-54 triangle.
 */
public final class CurvedArrow {

  private CurvedArrow() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Creates a new curved arrow.
   *
   * @param startAngle the starting angle of the arc, in radians
   * @param radius     the radius of the arrow. Must be non-negative.
   * @param length     the length of the arrow, in the same units as {@code radius}
   * @param xOffset    how much to offset the arc along the X-axis
   * @param headSize   the length of the head of the arrow
   *
   * @return a curved arrow shape
   *
   * @throws IllegalArgumentException if {@code radius} or {@code headSize} are negative
   */
  public static Shape create(double startAngle,
                             double radius,
                             double length,
                             double xOffset,
                             double headSize) {
    if (radius < 0) {
      throw new IllegalArgumentException("Radius cannot be negative. Given: " + radius);
    }
    if (headSize < 0) {
      throw new IllegalArgumentException("The size of the arrowhead cannot be negative. Given: " + headSize);
    }
    return Shape.union(
        makeBody(startAngle, radius, length, xOffset),
        makeHead(startAngle, headSize, radius, xOffset, length)
    );
  }

  /**
   * Creates a new curved arrow. This is equivalent to calling
   * {@link #create create(startAngle, radius, radius * sweepAngle, xOffset, headSize)}.
   *
   * @param startAngle the starting angle of the arc, in radians
   * @param radius     the radius of the arc
   * @param sweepAngle the sweep of the arc, in radians
   * @param xOffset    how much to offset the arc along the X-axis
   * @param headSize   the length of the head of the arrow
   *
   * @throws IllegalArgumentException if {@code radius} or {@code headSize} are negative
   */
  public static Shape createPolar(double startAngle,
                                  double radius,
                                  double sweepAngle,
                                  double xOffset,
                                  double headSize) {
    return create(startAngle, radius, radius * sweepAngle, xOffset, headSize);
  }

  /**
   * Generates the body arc of the arrow.
   *
   * @param startAngle the starting angle of the arc, in radians
   * @param radius     the radius of the arc
   * @param length     the length of the arc, in the same unit as {@code radius}
   * @param xOffset    how much to offset the arc along the X-axis
   */
  private static Arc makeBody(double startAngle, double radius, double length, double xOffset) {
    final double angRad = length / radius; // Isn't math nice?
    final double angle = Math.toDegrees(angRad);

    Arc arc = new Arc();
    arc.setRadiusX(radius);
    arc.setRadiusY(radius);
    arc.setCenterX(xOffset);
    arc.setCenterY(0);
    arc.setStartAngle(Math.toDegrees(startAngle));
    arc.setLength(-angle); // -angle because +y is "down", but JavaFX Arc treats +y as "up"

    arc.setFill(null);
    arc.setStroke(Color.WHITE);

    return arc;
  }

  /**
   * Generates the head of the arrow.
   *
   * @param startAngle the starting angle of the arc, in radians
   * @param size       the length of the arrow head
   * @param arcRadius  the radius of the arc of the arrow
   * @param arcLength  the length of the arc of the arrow
   */
  private static Triangle makeHead(double startAngle, double size, double arcRadius, double xOffset, double arcLength) {
    // Half the length of the triangle
    final double base = size / 2;

    // Angle to the base of the arrow
    final double angleToBase = arcLength / arcRadius - startAngle;

    // Radius to the tip of the arrow. Simple Pythagorean theorem
    final double tipRadius = Math.sqrt((size * size) + (arcRadius * arcRadius));

    // Angle to the tip of the arrow
    // If the length is negative, the tip is closer to the start of the arc and therefore needs to be
    // (arcLength - size) / arcRadius - startAngle
    // If the length is positive, the tip is further from the start of the arc is is therefore
    // (arcLength + size) / arcRadius - startAngle
    // These are combined into a single calculating by multiplying `size` by the sign of the arc length
    final double angleToTip = (arcLength + size * Math.signum(arcLength)) / arcRadius - startAngle;

    final double ux = Math.cos(angleToBase); // unit X in (-1, 1)
    final double uy = Math.sin(angleToBase); // unit Y in (-1, 1)

    // Note there's no "top" or "bottom" point, since it depends on the sign of uy
    Point2D basePoint1 = new Point2D((arcRadius + base) * ux + xOffset, (arcRadius + base) * uy);
    Point2D basePoint2 = new Point2D((arcRadius - base) * ux + xOffset, (arcRadius - base) * uy);
    Point2D tipPoint = new Point2D(tipRadius * Math.cos(angleToTip) + xOffset, tipRadius * Math.sin(angleToTip));

    Triangle triangle = new Triangle(basePoint1, tipPoint, basePoint2);

    triangle.setFill(Color.WHITE);
    triangle.setStroke(null);

    return triangle;
  }

  private static final class Triangle extends Polygon {

    public Triangle(Point2D a, Point2D b, Point2D c) {
      super(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY());
    }

  }

}
