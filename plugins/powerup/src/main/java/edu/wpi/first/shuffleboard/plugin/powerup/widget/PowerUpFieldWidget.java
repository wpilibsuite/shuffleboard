package edu.wpi.first.shuffleboard.plugin.powerup.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.Alliance;
import edu.wpi.first.shuffleboard.plugin.base.data.fms.FmsInfo;
import edu.wpi.first.shuffleboard.plugin.powerup.FieldConfiguration;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

@Description(name = "POWER UP Field", dataTypes = FmsInfo.class)
@ParametrizedController("PowerUpFieldWidget.fxml")
public class PowerUpFieldWidget extends SimpleAnnotatedWidget<FmsInfo> {

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

    root.scaleXProperty().bind(
        EasyBind.monadic(root.parentProperty())
            .map(p -> (Region) p)
            .flatMap(p -> EasyBind.combine(p.widthProperty(), p.heightProperty(), Size::new))
            .map(size -> calculateScaleFactor(size))
            .orElse(1.0));
    root.scaleYProperty().bind(root.scaleXProperty());
  }

  @Override
  public Pane getView() {
    return root;
  }

  private double calculateScaleFactor(Size size) {
    double widgetHeight = root.getHeight();
    double widgetWidth = root.getWidth();
    double targetRatio = widgetWidth / widgetHeight;
    double width = size.width;
    double height = size.height;
    double ratio = width / height;
    if (ratio > targetRatio) {
      return height / widgetHeight;
    } else {
      return width / widgetWidth;
    }
  }

  private static final class Size {
    public final double width;
    public final double height;

    Size(Number width, Number height) {
      this(width.doubleValue(), height.doubleValue());
    }

    Size(double width, double height) {
      this.width = width;
      this.height = height;
    }
  }

}
