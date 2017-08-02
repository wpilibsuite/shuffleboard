package edu.wpi.first.shuffleboard.widget;

import com.google.common.primitives.Doubles;
import edu.wpi.first.shuffleboard.data.types.NumberType;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Progress Bar", dataTypes = NumberType.class)
@ParametrizedController("ProgressBar.fxml")
public class ProgressBar extends SimpleAnnotatedWidget<Number> {

  @FXML
  private Pane root;
  @FXML
  javafx.scene.control.ProgressBar progressBar;

  final DoubleProperty minValue = new SimpleDoubleProperty(this, "Min Value", 0);
  final DoubleProperty maxValue = new SimpleDoubleProperty(this, "Max Value", 1);
  final BooleanProperty allowIndeterminateValue = new SimpleBooleanProperty(this, "Allow Indeterminate", false);

  @FXML
  private void initialize() {
    progressBar.progressProperty().bind(Bindings.createDoubleBinding(this::calculateProgress,
        dataProperty(), minValue, maxValue, allowIndeterminateValue));
    exportProperties(minValue, maxValue, allowIndeterminateValue);
  }

  private double calculateProgress() {
    if (getData() == null) {
      return -1;
    }
    final double min = minValue.get();
    final double max = maxValue.get();

    if (min >= max) {
      return -1;
    }

    final double value = getData().doubleValue();
    final boolean allowIndeterminate = allowIndeterminateValue.get();

    if (allowIndeterminate && (value < min || value > max)) {
      return -1;
    }

    return (Doubles.constrainToRange(value, min, max) - min) / (max - min);
  }

  @Override
  public Pane getView() {
    return root;
  }

}
