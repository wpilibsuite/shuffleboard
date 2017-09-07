package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.NumberField;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/**
 * A widget for displaying data as text. This supports text, numbers, and booleans.
 */
@Description(
    name = "Text View",
    summary = "Display a value as text",
    dataTypes = {
        String.class, Number.class, Boolean.class
    })
@ParametrizedController("TextView.fxml")
public class TextView extends SimpleAnnotatedWidget<Object> {

  private final StringProperty text = new SimpleStringProperty(this, "text", "");

  @FXML
  private Pane root;
  @FXML
  private TextField textField;
  @FXML
  private NumberField numberField;

  @FXML
  private void initialize() {
    text.bind(EasyBind.map(dataProperty(), this::simpleToString));
    MonadicBinding<Boolean> isNumber = EasyBind.map(dataProperty(), d -> d instanceof Number).orElse(false);
    numberField.visibleProperty().bind(isNumber);
    textField.visibleProperty().bind(numberField.visibleProperty().not());

    text.addListener((__, oldText, newText) -> {
      textField.setText(newText);
      numberField.setText(newText);
    });

    textField.textProperty().addListener((__, oldText, newText) -> {
      if (getData() instanceof Boolean) {
        // TODO maybe disable boolean text entry entirely? No point in typing "true" or "false" every time
        // Especially since checkboxes and toggle buttons exist
        setData(Boolean.valueOf(newText));
      } else {
        setData(newText);
      }
    });
    numberField.numberProperty().addListener((__, oldNumber, newNumber) -> setData(newNumber));
  }

  @Override
  public Pane getView() {
    return root;
  }

  private String simpleToString(Object obj) {
    if (obj == null) {
      return "";
    }
    return obj.toString();
  }

}
