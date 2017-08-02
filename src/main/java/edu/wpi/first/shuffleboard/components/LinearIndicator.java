package edu.wpi.first.shuffleboard.components;

import org.controlsfx.control.RangeSlider;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;

public class LinearIndicator extends RangeSlider {

  private final DoubleProperty value = new SimpleDoubleProperty(this, "value", 0);

  @SuppressWarnings("JavadocMethod")
  public LinearIndicator() {
    getStyleClass().add("linear-indicator");
    value.addListener((__, prev, cur) -> {
      double currentValue = cur.doubleValue();
      if (currentValue < 0) {
        setLowValue(currentValue);
        setHighValue(0);
      } else {
        setLowValue(0);
        setHighValue(currentValue);
      }
    });
    setDisable(true);
    pseudoClassStateChanged(PseudoClass.getPseudoClass("disabled"), false);
  }

  public double getValue() {
    return value.get();
  }

  public DoubleProperty valueProperty() {
    return value;
  }

  public void setValue(double value) {
    this.value.set(value);
  }
}
