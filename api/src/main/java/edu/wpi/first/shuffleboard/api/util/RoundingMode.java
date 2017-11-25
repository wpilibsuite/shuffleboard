package edu.wpi.first.shuffleboard.api.util;

/**
 * Specifies a <i>rounding behavior</i> for mathematical operations.
 */
@FunctionalInterface
public interface RoundingMode {

  /**
   * Rounds a fractional value to the nearest integer.
   */
  RoundingMode NEAREST = value -> (int) Math.round(value);
  /**
   * Rounds a fractional value down to the nearest integer less than or equal to the given value.
   */
  RoundingMode UP = value -> (int) Math.ceil(value);
  /**
   * Rounds a fractional value up to the nearest integer greater than or equal to the given value.
   */
  RoundingMode DOWN = value -> (int) Math.floor(value);

  /**
   * Rounds a fractional value to an integer.
   *
   * @param value the value to round
   */
  int round(double value);


}
