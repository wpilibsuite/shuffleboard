package edu.wpi.first.shuffleboard.widget;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;

@Description(
    name = "Number Slider",
    dataTypes = DataType.Number)
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
    slider.valueProperty().bindBidirectional(data);
  }

  @Override
  public Pane getView() {
    return root;
  }
}
