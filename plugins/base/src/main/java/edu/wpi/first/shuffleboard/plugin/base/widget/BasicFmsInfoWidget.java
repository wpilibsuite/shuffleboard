package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.ControlWord;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.FmsInfo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

@Description(name = "Basic FMS Info", dataTypes = FmsInfo.class)
@ParametrizedController("BasicFmsInfoWidget.fxml")
public class BasicFmsInfoWidget extends SimpleAnnotatedWidget<FmsInfo> {

  @FXML
  private Pane root;
  @FXML
  private Label matchInfoLabel;
  @FXML
  private Label robotStateLabel;
  @FXML
  private Label fmsConnectionLabel;
  @FXML
  private Label driverStationConnectionLabel;
  @FXML
  private Pane emergencyStopPane;

  @FXML
  private void initialize() {
    matchInfoLabel.textProperty().bind(dataOrDefault.map(this::generateInfoText));

    fmsConnectionLabel.textProperty().bind(
        dataOrDefault
            .map(info -> "FMS " + (info.getFmsControlData().isFmsAttached() ? "connected" : "disconnected")));
    driverStationConnectionLabel.textProperty().bind(
        dataOrDefault
            .map(info -> "DriverStation " + (info.getFmsControlData().isDsAttached() ? "connected" : "disconnected")));
    fmsConnectionLabel.graphicProperty().bind(
        dataOrDefault
            .map(FmsInfo::getFmsControlData)
            .map(d -> d.isFmsAttached() ? checkMarkLabel() : crossLabel()));
    driverStationConnectionLabel.graphicProperty().bind(
        dataOrDefault
            .map(FmsInfo::getFmsControlData)
            .map(d -> d.isDsAttached() ? checkMarkLabel() : crossLabel()));

    robotStateLabel.textProperty().bind(
        dataOrDefault
            .map(info -> "Robot state: " + info.getFmsControlData().getControlState().name()));

    emergencyStopPane.managedProperty().bind(
        dataOrDefault
            .map(FmsInfo::getFmsControlData)
            .map(ControlWord::isEmergencyStopped));
    emergencyStopPane.visibleProperty().bind(emergencyStopPane.managedProperty());
  }

  private Label crossLabel() {
    Label label = new Label("✖");
    label.setStyle("-fx-text-fill: red;");
    return label;
  }

  private Label checkMarkLabel() {
    Label label = new Label("✔");
    label.setStyle("-fx-text-fill: green;");
    return label;
  }

  @Override
  public Pane getView() {
    return root;
  }

  private String generateInfoText(FmsInfo info) {
    return String.format("%s %s match %d%s",
        info.getEventName(),
        info.getMatchType().getHumanReadableName(),
        info.getMatchNumber(),
        generateReplayString(info));
  }

  private String generateReplayString(FmsInfo info) {
    return info.getReplayNumber() > 0 ? " (replay " + info.getReplayNumber() + ")" : "";
  }

}
