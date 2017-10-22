package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.ThreeAxisAccelerometerData;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

@Description(name = "3-Axis Accelerometer", dataTypes = ThreeAxisAccelerometerData.class)
@ParametrizedController("ThreeAxisAccelerometerWidget.fxml")
public class ThreeAxisAccelerometerWidget extends SimpleAnnotatedWidget<ThreeAxisAccelerometerData> {

  private enum Range {
    k2G(2),
    k4G(4),
    k8G(8),
    k16G(16);

    private final int magnitude;

    Range(int magnitude) {
      this.magnitude = magnitude;
    }

    public int getMagnitude() {
      return magnitude;
    }
  }

  private final Property<Range> range = new SimpleObjectProperty<>(this, "range", Range.k16G);
  private final BooleanProperty showText = new SimpleBooleanProperty(this, "showText", true);
  private final IntegerProperty numDecimals = new SimpleIntegerProperty(this, "numDecimals", 2);

  @FXML
  private Pane root;
  @FXML
  private LinearIndicator x;
  @FXML
  private LinearIndicator y;
  @FXML
  private LinearIndicator z;
  @FXML
  private Label xLabel;
  @FXML
  private Label yLabel;
  @FXML
  private Label zLabel;

  @FXML
  private void initialize() {
    x.minProperty().bind(EasyBind.monadic(range).map(Range::getMagnitude).map(this::negateInteger));
    x.maxProperty().bind(EasyBind.monadic(range).map(Range::getMagnitude));

    x.majorTickUnitProperty().bind(EasyBind.monadic(range).map(Range::getMagnitude).map(i -> i / 2.0));

    x.valueProperty().bind(dataOrDefault.map(ThreeAxisAccelerometerData::getX));
    y.valueProperty().bind(dataOrDefault.map(ThreeAxisAccelerometerData::getY));
    z.valueProperty().bind(dataOrDefault.map(ThreeAxisAccelerometerData::getZ));

    xLabel.textProperty().bind(EasyBind.combine(x.valueProperty(), numDecimals, this::format));
    yLabel.textProperty().bind(EasyBind.combine(y.valueProperty(), numDecimals, this::format));
    zLabel.textProperty().bind(EasyBind.combine(z.valueProperty(), numDecimals, this::format));

    exportProperties(range, showText, numDecimals, x.showTickMarksProperty());
  }

  private String format(Number value, Number numDecimalPlaces) {
    return String.format("%." + numDecimalPlaces.intValue() + "f g", value.doubleValue());
  }

  private int negateInteger(int i) {
    return -i;
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
