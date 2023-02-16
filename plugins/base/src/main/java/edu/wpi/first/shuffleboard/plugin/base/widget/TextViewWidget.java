package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.NumberField;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.AnalogInputData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.AnalogInputType;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import org.fxmisc.easybind.EasyBind;

/** A widget for displaying data as text. This supports text, numbers, and booleans. */
@Description(
    name = "Text View",
    summary = "Display a value as text",
    dataTypes = {String.class, Number.class, Boolean.class, AnalogInputType.class})
@ParametrizedController("TextViewWidget.fxml")
public class TextViewWidget extends SimpleAnnotatedWidget<Object> {

  @FXML private Pane root;
  @FXML private TextField textField;
  @FXML private NumberField numberField;

  @FXML
  private void initialize() {
    dataProperty()
        .addListener(
            (__, prev, cur) -> {
              if (cur != null) {
                if (cur instanceof Number) {
                  numberField.setNumber(((Number) cur).doubleValue());
                } else if (cur instanceof String || cur instanceof Boolean) {
                  textField.setText(cur.toString());
                } else if (cur instanceof AnalogInputData) {
                  numberField.setNumber(((AnalogInputData) cur).getValue());
                } else {
                  throw new UnsupportedOperationException(
                      "Unsupported type: " + cur.getClass().getName());
                }
              }
            });
    numberField
        .visibleProperty()
        .bind(
            EasyBind.map(dataProperty(), d -> d instanceof Number || d instanceof AnalogInputData)
                .orElse(false));
    textField.visibleProperty().bind(numberField.visibleProperty().not());

    textField
        .textProperty()
        .addListener(
            (__, oldText, newText) -> {
              if (getData() instanceof Boolean) {
                // TODO maybe disable boolean text entry entirely? No point in typing "true" or
                // "false" every time
                // Especially since checkboxes and toggle buttons exist
                setData(Boolean.valueOf(newText));
              } else {
                setData(newText);
              }
            });
    numberField
        .numberProperty()
        .addListener(
            (__, oldNumber, newNumber) -> {
              if (newNumber != null) {
                if (getData() instanceof Number
                    || getData() instanceof String
                    || getData() instanceof Boolean) {
                  setData(newNumber);
                } else if (getData() instanceof AnalogInputData) {
                  // Analog inputs can't accept data
                } else {
                  throw new UnsupportedOperationException(
                      "Unsupported type: " + getData().getClass().getName());
                }
              }
            });
  }

  @Override
  public Pane getView() {
    return root;
  }
}
