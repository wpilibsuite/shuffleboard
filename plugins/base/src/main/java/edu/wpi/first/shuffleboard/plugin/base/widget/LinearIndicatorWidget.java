package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import org.fxmisc.easybind.EasyBind;


import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

@Description(name = "Linear Indicator", dataTypes = {Number.class})
@ParametrizedController("LinearIndicator.fxml")
public class LinearIndicatorWidget extends SimpleAnnotatedWidget<Number> {

  @FXML
  private Pane root;
  @FXML
  private LinearIndicator indicator;
  @FXML
  private Label text;

  private final DoubleProperty numTicks = new SimpleDoubleProperty(this, "numTickMarks", 5);
  private final BooleanProperty showText = new SimpleBooleanProperty(this, "showText", true);

  @FXML
  private void initialize() {
    indicator.valueProperty().bind(dataOrDefault);
    text.textProperty().bind(EasyBind.map(dataOrDefault, n -> String.format("%.2f", n.doubleValue())));

    indicator.majorTickUnitProperty().bind(
        EasyBind.combine(indicator.minProperty(), indicator.maxProperty(), numTicks,
            (min, max, numTicks) -> {
              if (numTicks.intValue() > 1) {
                return (max.doubleValue() - min.doubleValue()) / (numTicks.intValue() - 1);
              } else {
                return max.doubleValue() - min.doubleValue();
              }
            }));

    exportProperties(indicator.minProperty(), indicator.maxProperty(), indicator.centerProperty(), numTicks, showText);
  }

  @Override
  public Pane getView() {
    return root;
  }

  public boolean isShowText() {
    return showText.get();
  }

  public BooleanProperty showTextProperty() {
    return showText;
  }

  public void setShowText(boolean showText) {
    this.showText.set(showText);
  }

}
