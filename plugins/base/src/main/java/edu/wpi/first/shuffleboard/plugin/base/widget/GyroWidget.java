package edu.wpi.first.shuffleboard.plugin.base.widget;

import eu.hansolo.medusa.Gauge;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.GyroData;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

@Description(name = "Gyro", dataTypes = GyroData.class)
@ParametrizedController("GyroWidget.fxml")
public class GyroWidget extends SimpleAnnotatedWidget<GyroData> {

  @FXML
  private Pane root;
  @FXML
  private Gauge gauge;
  @FXML
  private Label valueLabel;

  @FXML
  private void initialize() {
    gauge.valueProperty().bind(dataOrDefault.map(GyroData::getWrappedValue));
    valueLabel.textProperty().bind(dataOrDefault.map(GyroData::getValue).map(d -> String.format("%.2f°", d)));

    exportProperties(
        gauge.majorTickSpaceProperty(),
        gauge.startAngleProperty(),
        gauge.tickMarkRingVisibleProperty()
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

}
