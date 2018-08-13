package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.DataSourceUtils;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;

@Description(name = "Toggle Button", dataTypes = Boolean.class)
@ParametrizedController("ToggleButtonWidget.fxml")
public class ToggleButtonWidget extends SimpleAnnotatedWidget<Boolean> {

  @FXML
  private Pane root;
  @FXML
  private ToggleButton button;
  private MonadicBinding<String> simpleSourceName; // NOPMD use a field to avoid GC

  @FXML
  private void initialize() {
    simpleSourceName = EasyBind.monadic(sourceProperty())
        .map(DataSource::getName)
        .map(DataSourceUtils::baseName)
        .orElse("");
    button.selectedProperty().bindBidirectional(dataProperty());
    button.textProperty().bind(simpleSourceName);
  }

  @Override
  public Pane getView() {
    return root;
  }

}
