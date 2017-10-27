package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.util.PropertyUtils;

import java.util.Objects;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * A type of text field that only accepts valid numbers.
 */
public abstract class AbstractNumberField<N extends Number> extends TextField {

  private final Property<N> number = new SimpleObjectProperty<>(this, "number");

  protected AbstractNumberField() {
    super();
    setText("0");
    setNumber(getNumberFromText("0"));
    setTextFormatter(new TextFormatter<>(change -> {
      String text = change.getControlNewText();
      if (isStartOfNumber(text)) {
        return change;
      }
      return null;
    }));
    PropertyUtils.bindBidirectionalWithConverter(
        textProperty(),
        number,
        text -> isCompleteNumber(text) ? getNumberFromText(text) : getNumber(),
        num -> Objects.equals(num, getNumberFromText(getText())) ? getText() : num.toString());
  }

  protected AbstractNumberField(N initialValue) {
    this();
    setNumber(initialValue);
  }

  /**
   * Checks if the given string is a valid start to an acceptable number in text form.
   * This differs from {@link #isCompleteNumber(String) isCompleteNumber} because this checks if the
   * text is only a valid <i>beginning</i> of a string representation of a number. For example, this
   * method could accept a single "-" because it's a valid start to a negative number.
   */
  protected abstract boolean isStartOfNumber(String text);

  /**
   * Checks if the given string is a valid number acceptable by this text field.
   */
  protected abstract boolean isCompleteNumber(String text);

  /**
   * Converts text representation of a number to the number itself.
   *
   * @param text the text to parse
   */
  protected abstract N getNumberFromText(String text);

  public final N getNumber() {
    return number.getValue();
  }

  public final Property<N> numberProperty() {
    return number;
  }

  public final void setNumber(N number) {
    this.number.setValue(number);
  }

}
