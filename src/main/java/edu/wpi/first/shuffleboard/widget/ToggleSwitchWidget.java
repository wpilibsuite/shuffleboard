package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.data.types.BooleanType;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Toggle Switch", dataTypes = BooleanType.class)
@ParametrizedController("ToggleSwitch.fxml")
public class ToggleSwitchWidget extends SimpleAnnotatedWidget<Boolean> {

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
