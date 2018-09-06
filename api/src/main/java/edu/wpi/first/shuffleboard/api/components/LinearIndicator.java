package edu.wpi.first.shuffleboard.api.components;

import org.controlsfx.control.RangeSlider;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.css.PseudoClass;

/**
 * A component that uses a bar to display a number. The bar can originate from any point along its range, making this
 * a good way to display numbers that can be in a range (eg -1 to 1, centered at 0).
 *
 * <p>Note that this extends {@code RangeSlider} to take advantage of the code that controlsfx has already written.
 * This component is <i>not</i> controllable, and exists only to be used as a read-only view.
 */
public class LinearIndicator extends RangeSlider {

  private final DoubleProperty value = new SimpleDoubleProperty(this, "value", 0);
  private final DoubleProperty center = new SimpleDoubleProperty(this, "center", 0);

  @SuppressWarnings("JavadocMethod")
  public LinearIndicator() {
    getStyleClass().add("linear-indicator");
    value.addListener(__ -> setRangeBounds());
    center.addListener(__ -> setRangeBounds());
    maxProperty().addListener(__ -> setRangeBounds());
    minProperty().addListener(__ -> setRangeBounds());
    setDisable(true);
    pseudoClassStateChanged(PseudoClass.getPseudoClass("disabled"), false);
  }

  /**
   * Sets the low and high values as necessary based on changes to the current value or center properties.
   */
  private void setRangeBounds() {
    double currentValue = getValue();
    double center = getCenter();
    if (currentValue < center) {
      setLowValue(currentValue);
      setHighValue(center);
    } else {
      setLowValue(center);
      setHighValue(currentValue);
    }
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

  public double getCenter() {
    return center.get();
  }

  public DoubleProperty centerProperty() {
    return center;
  }

  public void setCenter(double center) {
    this.center.set(center);
  }
}
