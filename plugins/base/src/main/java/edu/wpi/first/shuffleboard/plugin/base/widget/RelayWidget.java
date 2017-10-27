package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.RelayData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.RelayType;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Relay Widget", dataTypes = RelayType.class)
@ParametrizedController("RelayWidget.fxml")
public class RelayWidget extends SimpleAnnotatedWidget<RelayData> {

  @FXML
  private Pane root;
  @FXML
  private javafx.scene.control.ToggleButton offButton;
  @FXML
  private javafx.scene.control.ToggleButton onButton;
  @FXML
  private javafx.scene.control.ToggleButton forwardButton;
  @FXML
  private javafx.scene.control.ToggleButton reverseButton;

  @FXML
  private void initialize() {
    offButton.setOnMouseClicked(event -> {
      if (dataProperty().get() != null) {
        dataProperty().setValue(dataProperty().get().withState(RelayData.State.OFF));
      }
      offButton.setSelected(true);
    });
    onButton.setOnMouseClicked(event -> {
      if (dataProperty().get() != null) {
        dataProperty().setValue(dataProperty().get().withState(RelayData.State.ON));
      }
      onButton.setSelected(true);
    });
    forwardButton.setOnMouseClicked(event -> {
      if (dataProperty().get() != null) {
        dataProperty().setValue(dataProperty().get().withState(RelayData.State.FORWARD));
      }
      forwardButton.setSelected(true);
    });
    reverseButton.setOnMouseClicked(event -> {
      if (dataProperty().get() != null) {
        dataProperty().setValue(dataProperty().get().withState(RelayData.State.REVERSE));
      }
      reverseButton.setSelected(true);
    });
    dataProperty().addListener((obs, oldValue, newValue) -> {
      RelayData.State state = newValue.getState();
      if (state == null) {
        offButton.setSelected(false);
        onButton.setSelected(false);
        forwardButton.setSelected(false);
        reverseButton.setSelected(false);
        return;
      }
      switch (state) {
        case OFF:
          offButton.setSelected(true);
          break;
        case ON:
          onButton.setSelected(true);
          break;
        case FORWARD:
          forwardButton.setSelected(true);
          break;
        case REVERSE:
          reverseButton.setSelected(true);
          break;
        default:
          throw new AssertionError("Impossible value for relay state.");
      }
    });
  }

  @Override
  public Pane getView() {
    return root;
  }

}
