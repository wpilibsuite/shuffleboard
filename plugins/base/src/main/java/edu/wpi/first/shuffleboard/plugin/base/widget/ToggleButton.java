package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import edu.wpi.first.networktables.NetworkTable;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Toggle Button", dataTypes = Boolean.class)
@ParametrizedController("ToggleButton.fxml")
public class ToggleButton extends SimpleAnnotatedWidget<Boolean> {

  @FXML
  private Pane root;
  @FXML
  private javafx.scene.control.ToggleButton button;
  private MonadicBinding<String> simpleSourceName; // NOPMD use a field to avoid GC

  @FXML
  private void initialize() {
    simpleSourceName = EasyBind.monadic(sourceProperty())
        .map(DataSource::getName)
        .map(NetworkTable::basenameKey)
        .orElse("");
    button.selectedProperty().bindBidirectional(dataProperty());
    button.textProperty().bind(simpleSourceName);
  }

  @Override
  public Pane getView() {
    return root;
  }

}
