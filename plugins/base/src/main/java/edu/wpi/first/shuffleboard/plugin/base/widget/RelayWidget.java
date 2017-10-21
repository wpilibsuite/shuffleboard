package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

@Description(name = "Relay Widget", dataTypes = String.class)
@ParametrizedController("RelayWidget.fxml")
public class RelayWidget extends SimpleAnnotatedWidget<String> {

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

  private enum State {
    OFF("Off"),
    ON("On"),
    FORWARD("Forward"),
    REVERSE("Reverse");

    private final String value;

    State(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    public static State fromString(String str) {
      for (State state : State.values()) {
        if (state.value.equals(str)) {
          return state;
        }
      }
      return null;
    }
  }

  @FXML
  private void initialize() {
    offButton.setOnMouseClicked(event -> {
      dataProperty().setValue(State.OFF.getValue());
      offButton.setSelected(true);
    });
    onButton.setOnMouseClicked(event -> {
      dataProperty().setValue(State.ON.getValue());
      onButton.setSelected(true);
    });
    forwardButton.setOnMouseClicked(event -> {
      dataProperty().setValue(State.FORWARD.getValue());
      forwardButton.setSelected(true);
    });
    reverseButton.setOnMouseClicked(event -> {
      dataProperty().setValue(State.REVERSE.getValue());
      reverseButton.setSelected(true);
    });
    dataProperty().addListener((obs, oldValue, newValue) -> {
      State state = State.fromString(newValue);
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
          throw new AssertionError();
      }
    });
  }

  @Override
  public Pane getView() {
    return root;
  }

}
