package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.NumberField;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.PIDControllerData;

import org.controlsfx.control.ToggleSwitch;

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
  private NumberField fField;
  @FXML
  private NumberField setpointField;
  @FXML
  private ToggleSwitch enableToggle;

  private boolean togglingFromDataChange = true;

  @FXML
  private void initialize() {
    root.setStyle("-fx-font-size: 10pt;");
    dataProperty().addListener((__, old, newData) -> {
      pField.setNumber(newData.getP());
      iField.setNumber(newData.getI());
      dField.setNumber(newData.getD());
      fField.setNumber(newData.getF());
      setpointField.setNumber(newData.getSetpoint());
      togglingFromDataChange = true;
      enableToggle.setSelected(newData.isEnabled());
      togglingFromDataChange = false;
    });

    actOnFocusLost(pField);
    actOnFocusLost(iField);
    actOnFocusLost(dField);
    actOnFocusLost(fField);
    actOnFocusLost(setpointField);

    enableToggle.selectedProperty().addListener((__, prev, cur) -> {
      if (!togglingFromDataChange) {
        PIDControllerData data = getData();
        setData(data.withEnabled(!data.isEnabled()));
      }
    });
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
  private void setF() {
    setData(getData().withF(fField.getNumber()));
  }

  @FXML
  private void setSetpoint() {
    setData(getData().withSetpoint(setpointField.getNumber()));
  }

}
