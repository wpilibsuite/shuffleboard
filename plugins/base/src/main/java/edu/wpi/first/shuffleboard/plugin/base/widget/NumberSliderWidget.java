package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import com.google.common.collect.ImmutableList;

import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;

@Description(
    name = "Number Slider",
    dataTypes = Number.class)
@ParametrizedController("NumberSliderWidget.fxml")
public class NumberSliderWidget extends SimpleAnnotatedWidget<Number> {

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
    slider.valueProperty().bindBidirectional(dataProperty());
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Slider Settings",
            Setting.of("Min", slider.minProperty(), Double.class),
            Setting.of("Max", slider.maxProperty(), Double.class),
            Setting.of("Block increment", slider.blockIncrementProperty(), Double.class)
        )
    );
  }
}
