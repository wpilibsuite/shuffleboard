package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.components.LinearIndicator;
import edu.wpi.first.shuffleboard.data.AnalogInputData;
import edu.wpi.first.shuffleboard.data.types.AnalogInputType;
import edu.wpi.first.shuffleboard.data.types.NumberType;
import edu.wpi.first.shuffleboard.sources.DataSource;

import org.fxmisc.easybind.EasyBind;

import java.util.logging.Logger;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Voltage View", dataTypes = {NumberType.class, AnalogInputType.class})
@ParametrizedController("VoltageView.fxml")
public class VoltageViewWidget extends AnnotatedWidget {

  private static final Logger log = Logger.getLogger(VoltageViewWidget.class.getName());

  @FXML
  private Pane root;
  @FXML
  private LinearIndicator indicator;

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
    exportProperties(indicator.minProperty(), indicator.maxProperty(), indicator.orientationProperty());
  }

  @Override
  public Pane getView() {
    return root;
  }
}
