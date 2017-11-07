package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.UnitStringConverter;
import edu.wpi.first.shuffleboard.api.widget.AnnotatedWidget;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SingleSourceWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.AnalogInputData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.AnalogInputType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.NumberType;

import org.fxmisc.easybind.EasyBind;

import java.util.logging.Logger;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Voltage View", dataTypes = {NumberType.class, AnalogInputType.class})
@ParametrizedController("VoltageView.fxml")
public class VoltageViewWidget extends SingleSourceWidget implements AnnotatedWidget {

  private static final Logger log = Logger.getLogger(VoltageViewWidget.class.getName());

  @FXML
  private Pane root;
  @FXML
  private LinearIndicator indicator;

  private final DoubleProperty numTicks = new SimpleDoubleProperty(this, "numTickMarks", 5);

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
    exportProperties(indicator.minProperty(), indicator.maxProperty(), indicator.centerProperty(),
        indicator.orientationProperty(), numTicks);
  }

  @Override
  public Pane getView() {
    return root;
  }

}
