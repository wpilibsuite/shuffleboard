package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.sources.DataSource;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import org.fxmisc.easybind.EasyBind;

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
public class TextView extends SimpleWidget<Object> {

  private final StringProperty text = new SimpleStringProperty(this, "text", "");
  private final StringProperty label = new SimpleStringProperty(this, "label", "");


  @FXML
  private Pane root;

  public TextView() {
    textProperty().bind(
        EasyBind.select(sourceProperty())
                .selectObject(DataSource::dataProperty)
                .map(this::simpleToString)
    );
    labelProperty().bind(EasyBind.map(sourceNameProperty(), s -> s.isEmpty() ? "- No Source -" : s));
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

  public String getLabel() {
    return label.get();
  }

  public StringProperty labelProperty() {
    return label;
  }
}
