package edu.wpi.first.shuffleboard.app.widget;

import com.google.common.primitives.Doubles;
import edu.wpi.first.shuffleboard.api.data.types.NumberType;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
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

  final DoubleProperty minValue = new SimpleDoubleProperty(this, "Min Value", -1);
  final DoubleProperty maxValue = new SimpleDoubleProperty(this, "Max Value", 1);

  @FXML
  private void initialize() {
    progressBar.progressProperty().bind(Bindings.createDoubleBinding(this::calculateProgress,
        dataProperty(), minValue, maxValue));
    exportProperties(minValue, maxValue);
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

    return (Doubles.constrainToRange(value, min, max) - min) / (max - min);
  }

  @Override
  public Pane getView() {
    return root;
  }

}
