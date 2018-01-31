package edu.wpi.first.shuffleboard.plugin.powerup.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.Alliance;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.FmsInfo;
import edu.wpi.first.shuffleboard.plugin.powerup.FieldConfiguration;

import org.fxmisc.easybind.monadic.MonadicBinding;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Powerup Field", dataTypes = FmsInfo.class)
@ParametrizedController("PowerupFieldWidget.fxml")
public class PowerupFieldWidget extends SimpleAnnotatedWidget<FmsInfo> {

  @FXML
  private Pane root;
  @FXML
  private Pane nearSwitch;
  @FXML
  private Pane scale;
  @FXML
  private Pane farSwitch;

  private final MonadicBinding<FieldConfiguration> fieldConfiguration =
      dataOrDefault.map(FieldConfiguration::parseFmsInfo);

  @FXML
  private void initialize() {
    SwitchController nearSwitchController = (SwitchController) nearSwitch.getProperties().get("fx:controller");
    ScaleController scaleController = (ScaleController) scale.getProperties().get("fx:controller");
    SwitchController farSwitchController = (SwitchController) farSwitch.getProperties().get("fx:controller");

    nearSwitchController.configurationProperty().bind(fieldConfiguration.map(FieldConfiguration::getNearSwitch));
    scaleController.configurationProperty().bind(fieldConfiguration.map(FieldConfiguration::getScale));
    scaleController.allianceProperty().bind(
        dataOrDefault.filter(i -> i.getFmsControlData().toBits() != 0)
            .map(FmsInfo::getAlliance)
            .orElse((Alliance) null));
    farSwitchController.configurationProperty().bind(fieldConfiguration.map(FieldConfiguration::getFarSwitch));
  }

  @Override
  public Pane getView() {
    return root;
  }

}
