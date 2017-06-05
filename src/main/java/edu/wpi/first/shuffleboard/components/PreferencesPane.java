package edu.wpi.first.shuffleboard.components;

import com.google.common.collect.ImmutableList;
import javafx.beans.property.Property;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * A pane for editing some preferences.
 */
public class PreferencesPane extends GridPane {

  private static final Map<Class<?>, Function<Property<?>, Control>> editors = new HashMap<>();

  private final List<Control> editorControls = new ArrayList<>();
  private int row = 0; // NOPMD redundant field initializer

  public PreferencesPane() {
    getStyleClass().add("widgets-preferences-pane");
  }

  /**
   * Populates this preferences pane from a list of properties. Only properties that have defined
   * editors will appear in this pane.
   */
  public void populateFrom(List<Property<?>> properties) {
    row = 0;
    properties.stream()
              .filter(this::canEditProperty)
              .forEachOrdered(this::addRowForProperty);
  }

  /**
   * Gets the editor controls for this preferences pane.
   */
  public List<Control> getEditorControls() {
    return ImmutableList.copyOf(editorControls);
  }

  private boolean canEditProperty(Property<?> property) {
    final Class<?> propertyClass = property.getValue().getClass();
    return editors.containsKey(propertyClass);
  }

  private void addRowForProperty(Property<?> property) {
    Control control = makeControl(property);
    add(new Label(property.getName()), 0, row, 1, 1);
    add(control, 1, row, 1, 1);
    editorControls.add(control);
    row++;
  }

  private Control makeControl(Property<?> property) {
    final Class<?> propertyClass = property.getValue().getClass();
    return editors.get(propertyClass).apply(property);
  }

  // Store the editors

  static {
    setEditorForType(String.class, property -> {
      TextField textField = new TextField(property.getValue());
      textField.setOnAction(__ -> property.setValue(textField.getText()));
      return textField;
    });

    setEditorForType(Double.class, property -> {
      NumberField numberField = new NumberField(property.getValue());
      numberField.setOnAction(__ -> property.setValue(numberField.getNumber()));
      return numberField;
    });
  }

  /**
   * Sets the editor to use for the given type.
   *
   * @param type     the type of value to set the editor for
   * @param function the function to call to create an editor for a property of the given type
   */
  @SuppressWarnings("unchecked")
  public static <T> void setEditorForType(Class<T> type, Function<Property<T>, Control> function) {
    editors.put(type, (Function) function);
  }

}
