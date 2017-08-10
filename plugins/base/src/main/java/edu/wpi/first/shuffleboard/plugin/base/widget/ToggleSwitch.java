package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Toggle Switch", dataTypes = Boolean.class)
@ParametrizedController("ToggleSwitch.fxml")
public class ToggleSwitch extends SimpleAnnotatedWidget<Boolean> {

  @FXML
  private Pane root;
  @FXML
  private org.controlsfx.control.ToggleSwitch toggleSwitch;

  @FXML
  private void initialize() {
    toggleSwitch.selectedProperty().bindBidirectional(dataProperty());
  }

  @Override
  public Pane getView() {
    return root;
  }

}
