package edu.wpi.first.shuffleboard.plugin.base.widget;

import eu.hansolo.medusa.Gauge;

import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.GyroData;

import com.google.common.collect.ImmutableList;

import java.util.List;

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
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Visuals",
            Setting.of("Major tick spacing", gauge.majorTickSpaceProperty(), Double.class),
            Setting.of("Starting angle", gauge.startAngleProperty(), Double.class),
            Setting.of("Show tick mark ring", gauge.tickMarkRingVisibleProperty(), Boolean.class)
        )
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

}
