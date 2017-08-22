package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;

@Description(
    name = "Number Slider",
    dataTypes = Number.class)
@ParametrizedController("NumberSlider.fxml")
public class NumberSlider extends SimpleAnnotatedWidget<Number> {

  @FXML
  private Pane root;
  @FXML
  private Slider slider;

  @FXML
  private void initialize() {
    // enforce five evenly-spaced ticks at all times
    slider.majorTickUnitProperty().bind(
        slider.maxProperty()
              .subtract(slider.minProperty())
              .divide(4));
    exportProperties(slider.minProperty(), slider.maxProperty(), slider.blockIncrementProperty());
    slider.valueProperty().bindBidirectional(dataProperty());
  }

  @Override
  public Pane getView() {
    return root;
  }
}
