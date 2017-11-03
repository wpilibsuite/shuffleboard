package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.Pane;

@Description(name = "Command", dataTypes = CommandData.class)
@ParametrizedController("Command.fxml")
public class CommandWidget extends SimpleAnnotatedWidget<CommandData> {

  @FXML
  private Pane root;
  @FXML
  private CheckBox checkBox;

  @FXML
  private void initialize() {
    dataProperty().addListener((__, oldData, newData) -> checkBox.setSelected(newData.isRunning()));
    checkBox.selectedProperty().addListener((__, was, is) -> setData(getData().withRunning(is)));
  }

  @Override
  public Pane getView() {
    return root;
  }

}
