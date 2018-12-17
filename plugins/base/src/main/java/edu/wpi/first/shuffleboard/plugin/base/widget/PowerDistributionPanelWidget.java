package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.LinearIndicator;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.UnitStringConverter;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.PowerDistributionData;

import com.google.common.collect.ImmutableList;

import java.util.List;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

@Description(name = "PDP", dataTypes = PowerDistributionData.class)
@ParametrizedController("PowerDistributionPanel.fxml")
@SuppressWarnings("PMD.TooManyFields")
public class PowerDistributionPanelWidget extends SimpleAnnotatedWidget<PowerDistributionData> {

  private static final UnitStringConverter voltConverter = new UnitStringConverter("V");
  private static final UnitStringConverter ampConverter = new UnitStringConverter("A");

  private final BooleanProperty showIndicatorText = new SimpleBooleanProperty(this, "showIndicatorText", true);

  @FXML
  private Pane root;
  @FXML
  private ImageView imageView;
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
  @FXML
  private Label voltageText;
  @FXML
  private Label totalCurrentText;

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

    imageView.fitHeightProperty().bind(root.heightProperty().multiply(0.6));

    dataProperty().addListener((__, oldData, newData) -> {
      double[] currents = newData.getCurrents();
      for (int i = 0; i < currents.length; i++) {
        double current = currents[i];
        FxUtils.getLabel(channels[i]).ifPresent(label -> label.setText(ampConverter.toString(current)));
        channels[i].setValue(current);
      }
      voltage.setValue(newData.getVoltage());
      voltageText.setText(voltConverter.toString(newData.getVoltage()));
      totalCurrent.setValue(newData.getTotalCurrent());
      totalCurrentText.setText(ampConverter.toString(newData.getTotalCurrent()));
    });
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Visuals",
            Setting.of("Show voltage and current values", showIndicatorText, Boolean.class)
        )
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

  public boolean isShowIndicatorText() {
    return showIndicatorText.get();
  }

  public BooleanProperty showIndicatorTextProperty() {
    return showIndicatorText;
  }

  public void setShowIndicatorText(boolean showIndicatorText) {
    this.showIndicatorText.set(showIndicatorText);
  }

}
