package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.property.ReadOnlyStringProperty;
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
        String.class, Number.class, Boolean.class
    })
@ParametrizedController("TextView.fxml")
public class TextView extends SimpleAnnotatedWidget<Object> {

  private final StringProperty text = new SimpleStringProperty(this, "text", "");
  private final StringProperty label = new SimpleStringProperty(this, "label", "");

  @FXML
  private Pane root;

  /**
   * Creates a TextView widget.
   */
  public TextView() {
    text.bind(
        EasyBind.select(sourceProperty())
                .selectObject(DataSource::dataProperty)
                .map(this::simpleToString)
    );
    label.bind(
            EasyBind.map(sourceNameProperty(), name -> name.isEmpty() ? "- No Source -" : name)
    );
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

  public ReadOnlyStringProperty textProperty() {
    return text;
  }

  public void setText(String text) {
    this.text.set(text);
  }

  public String getLabel() {
    return label.get();
  }

  public ReadOnlyStringProperty labelProperty() {
    return label;
  }
}
