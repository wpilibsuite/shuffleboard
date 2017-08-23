package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.PowerDistributionData;

import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "PDP", dataTypes = PowerDistributionData.class)
@ParametrizedController("PowerDistributionPanel.fxml")
public class PowerDistributionPanelWidget extends SimpleAnnotatedWidget<PowerDistributionData> {

  @FXML
  private Pane root;
  @FXML
  private LinearIndicator channel0;
  @FXML
  private LinearIndicator channel1;
  @FXML
  private LinearIndicator channel2;
  @FXML
  private LinearIndicator channel3;
  @FXML
  private LinearIndicator channel4;
  @FXML
  private LinearIndicator channel5;
  @FXML
  private LinearIndicator channel6;
  @FXML
  private LinearIndicator channel7;
  @FXML
  private LinearIndicator channel8;
  @FXML
  private LinearIndicator channel9;
  @FXML
  private LinearIndicator channel10;
  @FXML
  private LinearIndicator channel11;
  @FXML
  private LinearIndicator channel12;
  @FXML
  private LinearIndicator channel13;
  @FXML
  private LinearIndicator channel14;
  @FXML
  private LinearIndicator channel15;
  @FXML
  private LinearIndicator voltage;
  @FXML
  private LinearIndicator totalCurrent;

  // Arrange the channel indicators in an array for ease of use
  private LinearIndicator[] channels;

  @FXML
  private void initialize() {
    channels = new LinearIndicator[]{
        channel0,
        channel1,
        channel2,
        channel3,
        channel4,
        channel5,
        channel6,
        channel7,
        channel8,
        channel9,
        channel10,
        channel11,
        channel12,
        channel13,
        channel14,
        channel15
    };

    dataProperty().addListener((__, oldData, newData) -> {
      double[] currents = newData.getCurrents();
      for (int i = 0; i < currents.length; i++) {
        channels[i].setValue(currents[i]);
      }
      voltage.setValue(newData.getVoltage());
      totalCurrent.setValue(newData.getTotalCurrent());
    });
  }

  @Override
  public Pane getView() {
    return root;
  }

}
