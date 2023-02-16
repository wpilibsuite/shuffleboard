package edu.wpi.first.shuffleboard.plugin.base.widget;

import com.google.common.collect.ImmutableList;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.GyroData;
import eu.hansolo.medusa.Gauge;
import java.util.List;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

@Description(
    name = "Gyro",
    dataTypes = {GyroData.class, Number.class})
@ParametrizedController("GyroWidget.fxml")
public class GyroWidget extends SimpleAnnotatedWidget<Object> {

  @FXML private Pane root;
  @FXML private Gauge gauge;
  @FXML private Label valueLabel;

  @FXML
  private void initialize() {
    dataProperty()
        .addListener(
            (__, prev, cur) -> {
              if (cur != null) {
                double angle = 0;
                if (cur instanceof Number) {
                  angle = ((Number) cur).doubleValue();
                } else if (cur instanceof GyroData) {
                  angle = ((GyroData) cur).getWrappedValue();
                }
                angle = wrapAngle(angle);

                gauge.setValue(angle);
                valueLabel.setText(String.format("%.2f", angle));
              }
            });
  }

  /**
   * Helper method to keep an angle in the range [0, 360). eg wrapAngle(90) -> 90 wrapAngle(360) ->
   * 0 wrapAngle(-15) -> 345
   */
  private double wrapAngle(double angle) {
    if (angle < 0) {
      return ((angle % 360) + 360) % 360;
    } else {
      return angle % 360;
    }
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of(
            "Visuals",
            Setting.of("Major tick spacing", gauge.majorTickSpaceProperty(), Double.class),
            Setting.of("Starting angle", gauge.startAngleProperty(), Double.class),
            Setting.of("Show tick mark ring", gauge.tickMarkRingVisibleProperty(), Boolean.class),
            Setting.of("Counter clockwise", createCounterClockwiseProperty(), Boolean.class)));
  }

  @Override
  public Pane getView() {
    return root;
  }

  private Property<Boolean> createCounterClockwiseProperty() {
    return new BooleanPropertyBase() {
      @Override
      public boolean get() {
        set(gauge.getScaleDirection() == Gauge.ScaleDirection.COUNTER_CLOCKWISE);
        return super.get();
      }

      @Override
      public void set(boolean newValue) {
        super.set(newValue);
        gauge.setScaleDirection(
            newValue ? Gauge.ScaleDirection.COUNTER_CLOCKWISE : Gauge.ScaleDirection.CLOCKWISE);
      }

      @Override
      public Object getBean() {
        return gauge;
      }

      @Override
      public String getName() {
        return "counterClockwise";
      }
    };
  }
}
