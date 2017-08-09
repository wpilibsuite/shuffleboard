package edu.wpi.first.shuffleboard.app.widget;

import edu.wpi.first.shuffleboard.api.data.types.BooleanType;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Toggle Button", dataTypes = BooleanType.class)
@ParametrizedController("ToggleButton.fxml")
public class ToggleButton extends SimpleAnnotatedWidget<Boolean> {

  @FXML
  private Pane root;
  @FXML
  private javafx.scene.control.ToggleButton button;

  @FXML
  private void initialize() {
    button.selectedProperty().bindBidirectional(dataProperty());
    button.textProperty().bind(sourceNameProperty());
  }

  @Override
  public Pane getView() {
    return root;
  }

}
