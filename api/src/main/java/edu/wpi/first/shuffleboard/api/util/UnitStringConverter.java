package edu.wpi.first.shuffleboard.api.util;

import javafx.beans.NamedArg;
import javafx.util.StringConverter;

/**
 * A string converter that converts numbers to formatted unit strings. The unit strings are appended to the number
 * text; for example, "12 Meters" or "-23.25 Volts". There is always a space between the number and the unit, and
 * either 2 decimal points ("X.XX") or none, if the number is an integer value.
 */
public class UnitStringConverter extends StringConverter<Number> {

  private final String unit;

  /**
   * Creates a new converter that uses the given unit string.
   *
   * @param unit the unit string to use, such as "Volts" or "Feet"
   */
  public UnitStringConverter(@NamedArg("unit") String unit) {
    this.unit = unit;
  }

  @Override
  public String toString(Number number) {
    if ((double) number.longValue() == number.doubleValue()) {
      // It's an integer value, don't show decimal point
      return String.format("%d %s", number.longValue(), unit);
    }
    return String.format("%.2f %s", number.doubleValue(), unit);
  }

  @Override
  public Double fromString(String string) {
    return Double.valueOf(string.substring(0, string.length() - (unit.length() + 1)));
  }

}
