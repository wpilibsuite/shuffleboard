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
  private static final Pattern integerPattern = Pattern.compile("^[-+]?\\d*$");

  /**
   * Creates a new number field with no value.
   */
  public IntegerField() {
    super();
    setText("0"); // initial text to match the initial number
    setTextFormatter(new TextFormatter<>(change -> {
      String text = change.getControlNewText();
      if (isInteger(text)) {
        return change;
      }
      return null;
    }));
    // bind text <-> number, only changing one if the new value has actually updated
    PropertyUtils.bindBidirectionalWithConverter(
        textProperty(),
        number,
        text -> isInteger(text) ? getNumberFromText(text) : getInteger(),
        num -> num == getNumberFromText(getText()) ? getText() : num.toString());
  }

  private int getNumberFromText(String text) {
    return Integer.parseInt(text);
  }

  /**
   * Creates an IntegerField with the given initial value.
   *
   * @param value the initial value of the text field
   */
  public IntegerField(int value) {
    this();
    setInteger(value);
  }

  /**
   * Checks if the given string is a valid whole number.
   */
  private static boolean isInteger(String text) {
    return integerPattern.matcher(text).matches();
  }

  public final int getInteger() {
    return number.getValue();
  }

  public final Property<Integer> integerProperty() {
    return number;
  }

  public final void setInteger(int number) {
    this.number.setValue(number);
  }

}
