package edu.wpi.first.shuffleboard.widget;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Toggle Button", dataTypes = DataType.Boolean)
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
