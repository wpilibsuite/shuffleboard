package edu.wpi.first.shuffleboard.widget;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Toggle Switch", dataTypes = DataType.Boolean)
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
