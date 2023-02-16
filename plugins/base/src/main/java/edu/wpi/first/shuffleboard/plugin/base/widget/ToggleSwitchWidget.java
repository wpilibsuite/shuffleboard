package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import org.controlsfx.control.ToggleSwitch;

@Description(name = "Toggle Switch", dataTypes = Boolean.class)
@ParametrizedController("ToggleSwitchWidget.fxml")
public class ToggleSwitchWidget extends SimpleAnnotatedWidget<Boolean> {

  @FXML private Pane root;
  @FXML private ToggleSwitch toggleSwitch;

  @FXML
  private void initialize() {
    toggleSwitch.selectedProperty().bindBidirectional(dataProperty());
  }

  @Override
  public Pane getView() {
    return root;
  }
}
