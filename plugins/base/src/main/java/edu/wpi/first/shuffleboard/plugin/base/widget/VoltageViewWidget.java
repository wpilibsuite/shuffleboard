package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.data.types.NumberType;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.UnitStringConverter;
import edu.wpi.first.shuffleboard.api.widget.AnnotatedWidget;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SingleSourceWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.AnalogInputData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.AnalogInputType;

import com.google.common.collect.ImmutableList;

import org.fxmisc.easybind.EasyBind;

import java.util.List;
import java.util.logging.Logger;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Orientation;
import javafx.scene.layout.Pane;

@Description(name = "Voltage View", dataTypes = {NumberType.class, AnalogInputType.class})
@ParametrizedController("VoltageView.fxml")
public class VoltageViewWidget extends SingleSourceWidget implements AnnotatedWidget {

  private static final Logger log = Logger.getLogger(VoltageViewWidget.class.getName());

  @FXML
  private Pane root;
  @FXML
  private LinearIndicator indicator;

  private final IntegerProperty numTicks = new SimpleIntegerProperty(this, "numTickMarks", 5);

  @FXML
  private void initialize() {
    indicator.valueProperty().bind(EasyBind.combine(
        EasyBind.monadic(sourceProperty()).selectProperty(DataSource::dataProperty),
        indicator.minProperty(),
        indicator.maxProperty(),
        (data, min, max) -> {
          if (!getSource().isActive()) {
            return min.doubleValue();
          }
          double value;
          if (data instanceof Number) {
            value = ((Number) data).doubleValue();
          } else if (data instanceof AnalogInputData) {
            value = ((AnalogInputData) data).getValue();
          } else {
            value = 0;
            log.warning("Unexpected data: " + data + " (Source: " + getSource() + ")"); // NOPMD
          }
          return value;
        }));
    indicator.majorTickUnitProperty().bind(
        EasyBind.combine(indicator.minProperty(), indicator.maxProperty(), numTicks,
            (min, max, numTicks) -> {
              if (numTicks.intValue() > 1) {
                return (max.doubleValue() - min.doubleValue()) / (numTicks.intValue() - 1);
              } else {
                return max.doubleValue() - min.doubleValue();
              }
            }));
    indicator.setLabelFormatter(new UnitStringConverter("V")); // "Volts" is too long and causes clipping issues
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Range",
            Setting.of("Min", indicator.minProperty(), Double.class),
            Setting.of("Max", indicator.maxProperty(), Double.class),
            Setting.of("Center", indicator.centerProperty(), Double.class)
        ),
        Group.of("Visuals",
            Setting.of("Orientation", indicator.orientationProperty(), Orientation.class),
            Setting.of("Number of tick marks", numTicks, Integer.class)
        )
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

}
