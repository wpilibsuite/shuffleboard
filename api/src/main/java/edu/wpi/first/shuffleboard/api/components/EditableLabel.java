package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.util.PseudoClassProperty;

import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;

public class EditableLabel extends StackPane {
  private final PseudoClassProperty editing = new PseudoClassProperty(this, "editing");
  private final StringProperty text = new SimpleStringProperty(this, "text");

  /**
   * A text label that you can double click to edit.
   */
  public EditableLabel() {
    setMaxWidth(USE_PREF_SIZE);

    Label label = new Label();
    label.textProperty().bind(text);
    label.visibleProperty().bind(Bindings.not(editing));
    getChildren().add(label);

    TextField editField = new AutoSizedTextField();
    editField.visibleProperty().bind(editing);
    editField.textProperty().bindBidirectional(text);
    getChildren().add(editField);

    setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getClickCount() == 2) {
        editing.set(true);
      }
    });

    editField.setOnAction(__ -> editing.set(false));

    editField.focusedProperty().addListener((__, wasFocused, isFocused) -> {
      if (!isFocused) {
        editing.set(false);
      }
    });

    editing.addListener((__, wasEditing, isEditing) -> {
      if (isEditing) {
        editField.requestFocus();
      }
    });
  }

  public EditableLabel(Property<String> text) {
    this();
    textProperty().bindBidirectional(text);
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
