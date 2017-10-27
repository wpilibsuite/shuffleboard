package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import eu.hansolo.medusa.Gauge;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Simple Dial", dataTypes = Number.class)
@ParametrizedController("SimpleDialWidget.fxml")
public class SimpleDialWidget extends SimpleAnnotatedWidget<Number> {

  @FXML
  private Pane root;
  @FXML
  private Gauge dial;

  @FXML
  private void initialize() {
    dial.valueProperty().bind(dataOrDefault);

    exportProperties(dial.minValueProperty(), dial.maxValueProperty(), dial.valueVisibleProperty());
  }

  @Override
  public Pane getView() {
    return root;
  }

}
