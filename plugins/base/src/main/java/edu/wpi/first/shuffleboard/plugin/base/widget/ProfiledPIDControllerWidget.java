package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.NumberField;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.ProfiledPIDControllerData;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/**
 * A widget for controlling Profiled PID controllers. This gives control over the three PID constants and the controller
 * position goal.
 *
 * <p>Future updates to the sendable implementation for profiled PID controllers (such as support for goal velocity and
 * trapezoidal constraints) will result in more available controls in this widget.
 */
@Description(name = "Profiled PID Controller", dataTypes = ProfiledPIDControllerData.class)
@ParametrizedController("ProfiledPIDControllerWidget.fxml")
public class ProfiledPIDControllerWidget extends SimpleAnnotatedWidget<ProfiledPIDControllerData> {

  @FXML
  private Pane root;
  @FXML
  private NumberField pField;
  @FXML
  private NumberField iField;
  @FXML
  private NumberField dField;
  @FXML
  private NumberField goalField;

  @FXML
  private void initialize() {
    root.setStyle("-fx-font-size: 10pt;");
    dataProperty().addListener((__, old, newData) -> {
      pField.setNumber(newData.getP());
      iField.setNumber(newData.getI());
      dField.setNumber(newData.getD());
      goalField.setNumber(newData.getGoal());
    });

    actOnFocusLost(pField);
    actOnFocusLost(iField);
    actOnFocusLost(dField);
    actOnFocusLost(goalField);
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
  private void setGoal() {
    setData(getData().withGoal(goalField.getNumber()));
  }

}
