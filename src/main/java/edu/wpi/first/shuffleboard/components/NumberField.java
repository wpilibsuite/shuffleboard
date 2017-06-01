package edu.wpi.first.shuffleboard.components;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * A type of text field that only accepts valid floating-point decimal numbers.
 */
public class NumberField extends TextField {

  private Property<Double> number = new SimpleObjectProperty<>(this, "number", 0.0);

  /**
   * Creates a new number field with no value.
   */
  public NumberField() {
    super();
    setText("0.0"); // initial text to match the initial number
    setTextFormatter(new TextFormatter<>(change -> {
      String text = change.getControlNewText();
      // almost, but not quite, the same regex as isValidDouble
      // note the trailing \d* instead of \d+
      if (text.matches("^[-+]?\\d*\\.?\\d*$")) {
        return change;
      }
      return null;
    }));
    textProperty().addListener((__, oldText, newText) -> {
      if (isValidDouble(newText)) {
        setNumber(Double.parseDouble(newText));
      }
    });
    number.addListener((__, oldNumber, newNumber) -> {
      if (getText() == null || getText().isEmpty() || newNumber != Double.parseDouble(getText())) {
        // only update the text if the number didn't update from the text property
        setText(newNumber.toString());
      }
    });
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
   * @param string
   * @return
   */
  private static boolean isValidDouble(String string) {
    return string.matches("^[-+]?\\d*\\.?\\d+$");
  }

  public double getNumber() {
    return number.getValue();
  }

  public Property<Double> numberProperty() {
    return number;
  }

  public void setNumber(double number) {
    this.number.setValue(number);
  }

}
