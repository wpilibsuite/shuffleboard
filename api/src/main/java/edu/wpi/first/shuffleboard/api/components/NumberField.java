package edu.wpi.first.shuffleboard.api.components;

import java.util.regex.Pattern;
import java.util.Locale;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 * A type of text field that only accepts valid floating-point decimal numbers.
 */
public class NumberField extends AbstractNumberField<Double> {

  private static final Pattern startOfFloatingPointNumber = Pattern.compile("^[-+]?\\d*\\.?\\d*$");
  private static final Pattern completeFloatingPointNumber = Pattern.compile("^[-+]?\\d*\\.?\\d+$");
  private static final DecimalFormat textFromNumberFormat =
          new DecimalFormat("0", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

  /**
   * Creates a new number field with no value.
   */
  public NumberField() {
    super();
  }

  /**
   * Creates a number field with the given initial value.
   *
   * @param initialValue the initial value of the text field
   */
  public NumberField(double initialValue) {
    super(initialValue);
  }

  @Override
  protected Double getNumberFromText(String text) {
    return Double.valueOf(text);
  }

  @Override
  protected String getTextFromNumber(Double num) {
    return textFromNumberFormat.format(num);
  }

  @Override
  protected boolean isCompleteNumber(String text) {
    return completeFloatingPointNumber.matcher(text).matches();
  }

  @Override
  protected boolean isStartOfNumber(String text) {
    return startOfFloatingPointNumber.matcher(text).matches();
  }
}
