package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.components.NumberField;
import javafx.beans.property.Property;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * Editors for widget preferences.
 */
public class PrefsEditors {

  private static final Map<Class<?>, Function<Property<?>, Control>> editors = new HashMap<>();

  /**
   * Sets the editor to use for a property of the given type.
   *
   * @param type           the type of data the control should edit
   * @param editorFunction the function to use to create an editor for a specific property.
   * @param <T>            the type of data to edit
   */
  @SuppressWarnings("unchecked")
  public static <T> void setEditorForType(Class<T> type,
                                          Function<Property<T>, Control> editorFunction) {
    editors.put(type, (Function) editorFunction);
  }

  /**
   * Creates an editor for a specific property, using the editor defined for the type as set with
   * {@link #setEditorForType(Class, Function) setEditorForType}.
   *
   * @param type     the type of the data held by the property
   * @param property the property to create an editor for
   * @param <T>      the type of data in the property
   * @return an optional containing the editor for the property, or an empty optional if no editor
   *         could be created
   */
  public static <T> Optional<Control> createEditorFor(Class<T> type, Property<T> property) {
    return Optional.ofNullable(editors.get(type))
                   .map(function -> function.apply(property));
  }

  // static init to add the default editors
  // this is ugly as sin and needs to be refactored later
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

}
