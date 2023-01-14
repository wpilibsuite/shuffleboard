package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.NumberField;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.PIDControllerData;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/**
 * A widget for controlling PID controllers. This gives control over the four PIDF constants, the controller
 * setpoint, and whether or not the controller is enabled.
 */
@Description(name = "PID Controller", dataTypes = PIDControllerData.class)
@ParametrizedController("PIDControllerWidget.fxml")
public class PIDControllerWidget extends SimpleAnnotatedWidget<PIDControllerData> {

  @FXML
  private Pane root;
  @FXML
  private NumberField pField;
  @FXML
  private NumberField iField;
  @FXML
  private NumberField dField;
  @FXML
  private NumberField setpointField;

  @FXML
  private void initialize() {
    root.setStyle("-fx-font-size: 10pt;");
    dataProperty().addListener((__, old, newData) -> {
      pField.setNumber(newData.getP());
      iField.setNumber(newData.getI());
      dField.setNumber(newData.getD());
      setpointField.setNumber(newData.getSetpoint());
    });

    actOnFocusLost(pField);
    actOnFocusLost(iField);
    actOnFocusLost(dField);
    actOnFocusLost(setpointField);
  }

  private void actOnFocusLost(TextField field) {
    field.focusedProperty().addListener((__, was, is) -> {
      if (!is) {
        field.getOnAction().handle(new ActionEvent(this, field));
      }
    });
  }

  @Override
  public Pane getView() {
    return root;
  }

  @FXML
  private void setP() {
    setData(getData().withP(pField.getNumber()));
  }

  @FXML
  private void setI() {
    setData(getData().withI(iField.getNumber()));
  }

  @FXML
  private void setD() {
    setData(getData().withD(dField.getNumber()));
  }

  @FXML
  private void setSetpoint() {
    setData(getData().withSetpoint(setpointField.getNumber()));
  }

}
