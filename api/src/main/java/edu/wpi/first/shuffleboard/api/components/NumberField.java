package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.util.PropertyUtils;

import java.util.regex.Pattern;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * A type of text field that only accepts valid floating-point decimal numbers.
 */
public class NumberField extends TextField {

  private final Property<Double> number = new SimpleObjectProperty<>(this, "number", 0.0);
  private static final Pattern startOfNumber = Pattern.compile("^[-+]?\\d*\\.?\\d*$");
  private static final Pattern completeNumber = Pattern.compile("^[-+]?\\d*\\.?\\d+$");

  /**
   * Creates a new number field with no value.
   */
  public NumberField() {
    super();
    setText("0.0"); // initial text to match the initial number
    setTextFormatter(new TextFormatter<>(change -> {
      String text = change.getControlNewText();
      if (isStartOfNumber(text)) {
        return change;
      }
      return null;
    }));
    // bind text <-> number, only changing one if the new value has actually updated
    PropertyUtils.bindBidirectionalWithConverter(
        textProperty(),
        number,
        text -> isCompleteNumber(text) ? getNumberFromText(text) : getNumber(),
        num -> num == getNumberFromText(getText()) ? getText() : num.toString());
  }

  private double getNumberFromText(String text) {
    return Double.parseDouble(text);
  }

  /**
   * Creates a number field with the given initial value.
   *
   * @param value the initial value of the text field
   */
  public NumberField(double value) {
    this();
    setNumber(value);
  }

  /**
   * Checks if the given string is a valid floating-point decimal number.
   */
  private static boolean isCompleteNumber(String text) {
    return completeNumber.matcher(text).matches();
  }

  /**
   * Checks if the given string is a valid start to a floating-point decimal number in text form.
   * This differs from {@link #isCompleteNumber(String) isCompleteNumber} because this checks if the
   * text is only a valid <i>beginning</i> of a string representation of a number. For example, this
   * method would accept a single "-" because it's a valid start to a negative number.
   */
  private static boolean isStartOfNumber(String text) {
    return startOfNumber.matcher(text).matches();
  }

  public final double getNumber() {
    return number.getValue();
  }

  public final Property<Double> numberProperty() {
    return number;
  }

  public final void setNumber(double number) {
    this.number.setValue(number);
  }

}
