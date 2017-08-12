package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.util.PropertyUtils;

import java.util.regex.Pattern;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * A type of text field that only accepts valid whole numbers.
 */
public class IntegerField extends TextField {

  private final Property<Integer> number = new SimpleObjectProperty<>(this, "number", 0);
  private static final Pattern numberPattern = Pattern.compile("^[-+]?\\d*$");

  /**
   * Creates a new number field with no value.
   */
  public IntegerField() {
    super();
    setText("0"); // initial text to match the initial number
    setTextFormatter(new TextFormatter<>(change -> {
      String text = change.getControlNewText();
      if (isNumber(text)) {
        return change;
      }
      return null;
    }));
    // bind text <-> number, only changing one if the new value has actually updated
    PropertyUtils.bindBidirectionalWithConverter(
        textProperty(),
        number,
        text -> isNumber(text) ? getNumberFromText(text) : getNumber(),
        num -> num == getNumberFromText(getText()) ? getText() : num.toString());
  }

  private int getNumberFromText(String text) {
    return Integer.parseInt(text);
  }

  /**
   * Creates a number field with the given initial value.
   *
   * @param value the initial value of the text field
   */
  public IntegerField(int value) {
    this();
    setNumber(value);
  }

  /**
   * Checks if the given string is a valid floating-point decimal number.
   */
  private static boolean isNumber(String text) {
    return numberPattern.matcher(text).matches();
  }

  public final int getNumber() {
    return number.getValue();
  }

  public final Property<Integer> numberProperty() {
    return number;
  }

  public final void setNumber(int number) {
    this.number.setValue(number);
  }

}
