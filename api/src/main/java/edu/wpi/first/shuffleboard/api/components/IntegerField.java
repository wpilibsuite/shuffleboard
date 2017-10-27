package edu.wpi.first.shuffleboard.api.components;

import java.util.regex.Pattern;

/**
 * A type of text field that only accepts valid integers.
 */
public class IntegerField extends AbstractNumberField<Integer> {

  private static final Pattern startOfInteger = Pattern.compile("^[-+]?\\d*$");
  private static final Pattern completeInteger = Pattern.compile("^[-+]?\\d+$");

  public IntegerField() {
    super();
  }

  public IntegerField(int initialValue) {
    super(initialValue);
  }

  @Override
  protected boolean isStartOfNumber(String text) {
    return startOfInteger.matcher(text).matches();
  }

  @Override
  protected boolean isCompleteNumber(String text) {
    return completeInteger.matcher(text).matches();
  }

  @Override
  protected Integer getNumberFromText(String text) {
    return Integer.valueOf(text);
  }

}
