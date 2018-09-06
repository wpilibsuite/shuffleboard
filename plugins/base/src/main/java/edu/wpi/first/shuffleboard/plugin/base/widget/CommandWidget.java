package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.CommandData;

import org.fxmisc.easybind.EasyBind;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;

@Description(name = "Command", dataTypes = CommandData.class)
@ParametrizedController("Command.fxml")
public class CommandWidget extends SimpleAnnotatedWidget<CommandData> {

  @FXML
  private Pane root;
  @FXML
  private ToggleButton button;

  @FXML
  private void initialize() {
    button.textProperty().bind(EasyBind.monadic(dataOrDefault).map(CommandData::getName));
    dataProperty().addListener((__, oldData, newData) -> button.setSelected(newData.isRunning()));
    button.selectedProperty().addListener((__, was, is) -> setData(getData().withRunning(is)));
  }

  @Override
  public Pane getView() {
    return root;
  }

}
