package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.RelayData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.RelayType;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Pane;

@Description(name = "Relay Widget", dataTypes = RelayType.class)
@ParametrizedController("RelayWidget.fxml")
public class RelayWidget extends SimpleAnnotatedWidget<RelayData> {

  @FXML
  private Pane root;
  @FXML
  private ToggleButton offButton;
  @FXML
  private ToggleButton onButton;
  @FXML
  private ToggleButton forwardButton;
  @FXML
  private ToggleButton reverseButton;

  @FXML
  private void initialize() {
    setupButton(offButton, RelayData.State.OFF);
    setupButton(onButton, RelayData.State.ON);
    setupButton(forwardButton, RelayData.State.FORWARD);
    setupButton(reverseButton, RelayData.State.REVERSE);
    dataOrDefault.addListener((obs, oldValue, newValue) -> {
      root.setDisable(!newValue.isControllable());
      RelayData.State state = newValue.getState();
      if (state == null) {
        offButton.setSelected(true);
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

  private void setupButton(ToggleButton button, RelayData.State relayState) {
    button.setOnAction(event -> {
      if (button.isSelected()) {
        setData(dataOrDefault.get().withState(relayState));
      } else {
        setDefaultState();
      }
    });
  }

  private void setDefaultState() {
    setData(dataOrDefault.get().withState(RelayData.State.OFF));
  }

}
