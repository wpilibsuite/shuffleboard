package edu.wpi.first.shuffleboard.widget;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * A widget for displaying data as text. This supports text, numbers, and booleans.
 */
@Description(
    name = "Text View",
    summary = "Display a value as text",
    dataTypes = {
        DataType.String, DataType.Number, DataType.Boolean
    })
@ParametrizedController("TextView.fxml")
public class TextView extends Widget<Object> {

  private final StringProperty text = new SimpleStringProperty(this, "text", "");

  @FXML
  private Pane root;

  @FXML
  @Override
  public void initialize() {
    text.bind(Bindings.createStringBinding(() -> simpleToString(source.getData()),
                                           source.dataProperty()));
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

  public String getText() {
    return text.get();
  }

  public StringProperty textProperty() {
    return text;
  }

  public void setText(String text) {
    this.text.set(text);
  }

}
