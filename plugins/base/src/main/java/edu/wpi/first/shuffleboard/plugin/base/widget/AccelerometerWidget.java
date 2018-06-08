package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.AccelerometerData;

import com.google.common.collect.ImmutableList;

import org.fxmisc.easybind.EasyBind;

import java.util.List;

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
  }

  private String generateLabelText(AccelerometerData data, Number numDecimals) {
    return String.format("%." + numDecimals.intValue() + "f g", data.getValue());
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Visuals",
            Setting.of("Show text", showText),
            Setting.of("Precision", numDecimals),
            Setting.of("Show tick marks", indicator.showTickMarksProperty())
        ),
        Group.of("Range",
            Setting.of("Min", indicator.minProperty()),
            Setting.of("Max", indicator.maxProperty())
        )
    );
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
