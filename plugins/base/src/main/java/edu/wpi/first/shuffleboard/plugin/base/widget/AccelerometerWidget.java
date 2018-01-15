package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.AccelerometerData;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

@Description(name = "Accelerometer", dataTypes = AccelerometerData.class)
@ParametrizedController("AccelerometerWidget.fxml")
public class AccelerometerWidget extends SimpleAnnotatedWidget<AccelerometerData> {

  @FXML
  private Pane root;
  @FXML
  private LinearIndicator indicator;
  @FXML
  private Label label;

  private final BooleanProperty showText = new SimpleBooleanProperty(this, "showText", true);
  private final IntegerProperty numDecimals = new SimpleIntegerProperty(this, "numDecimals", 2);

  @FXML
  private void initialize() {
    indicator.valueProperty().bind(dataOrDefault.map(AccelerometerData::getValue));
    label.textProperty().bind(EasyBind.combine(dataOrDefault, numDecimals, this::generateLabelText));
    exportProperties(
        showText,
        numDecimals,
        indicator.showTickMarksProperty(),
        indicator.minProperty(),
        indicator.maxProperty()
    );
  }

  private String generateLabelText(AccelerometerData data, Number numDecimals) {
    return String.format("%." + numDecimals.intValue() + "f g", data.getValue());
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
