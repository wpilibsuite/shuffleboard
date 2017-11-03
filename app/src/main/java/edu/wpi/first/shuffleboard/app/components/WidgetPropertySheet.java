package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.components.IntegerField;
import edu.wpi.first.shuffleboard.api.components.NumberField;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.theme.Themes;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * A property sheet for a specific widget.
 */
public class WidgetPropertySheet extends PropertySheet {

  /**
   * Creates a new property sheet for the given widget.
   */
  public WidgetPropertySheet(List<Property<?>> properties) {
    super(properties.stream()
        .map(property -> new PropertyItem<>(property))
        .collect(Collectors.toCollection(FXCollections::observableArrayList)));
    setModeSwitcherVisible(false);
    setSearchBoxVisible(false);
    setPropertyEditorFactory(new DefaultPropertyEditorFactory() {
      @Override
      public PropertyEditor<?> call(Item item) {
        if (item.getType() == String.class) {
          return new TextPropertyEditor(item);
        }
        if (item.getType() == Integer.class) {
          return new IntegerPropertyEditor(item);
        }
        if (Number.class.isAssignableFrom(item.getType())) {
          return new NumberPropertyEditor(item);
        }
        if (item.getType() == Boolean.class) {
          return new ToggleSwitchEditor(item);
        }
        if (item.getType() == Theme.class) {
          return new ThemePropertyEditor(item);
        }
        return super.call(item);
      }
    });
  }

  /**
   * An item backed by a JavaFX property.
   */
  public static class PropertyItem<T> implements Item {

    private final Property<T> property;
    private final String name;

    public PropertyItem(Property<T> property) {
      this.property = property;
      this.name = camelCaseToSentence(property.getName());
    }

    public PropertyItem(Property<T> property, String name) {
      this.property = property;
      this.name = name;
    }

    /**
     * Converts a "CamelCase" string to "Sentence case".
     */
    private static String camelCaseToSentence(String camel) {
      if (camel == null) {
        return null;
      } else if (camel.isEmpty()) {
        return "";
      }
      final char[] chars = camel.toCharArray();
      StringBuilder builder = new StringBuilder();
      builder.append(Character.toUpperCase(chars[0]));
      for (int i = 1; i < chars.length; i++) {
        char next = chars[i];
        if (Character.isUpperCase(next)) {
          builder.append(' ').append(Character.toLowerCase(next));
        } else {
          builder.append(next);
        }
      }
      return builder.toString();
    }

    @Override
    public Class<?> getType() {
      return property.getValue().getClass();
    }

    @Override
    public String getCategory() {
      return "Widget Properties";
    }

    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getDescription() {
      return "";
    }

    @Override
    public Object getValue() {
      return property.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setValue(Object value) {
      if (getType().isInstance(value)) {
        property.setValue((T) value);
      } else {
        throw new IllegalArgumentException(
            String.format("Cannot set value to %s (expected a %s, but was a %s)",
                value, getType().getName(), value.getClass().getName()));
      }
    }

    @Override
    public Optional<ObservableValue<?>> getObservableValue() {
      return Optional.of(property);
    }

  }


  private abstract static class AbstractEditor<T, C extends Control> extends AbstractPropertyEditor<T, C> {

    protected final BooleanProperty wait = new SimpleBooleanProperty(this, "wait", false);

    public AbstractEditor(Item property, C control) {
      super(property, control);
    }

  }

  /**
   * A property editor for numbers. We use this instead of the one bundled with ControlsFX because
   * their implementation is bad.
   */
  private static class NumberPropertyEditor extends AbstractEditor<Double, NumberField> {

    NumberPropertyEditor(Item item) {
      super(item, new NumberField(((Number) item.getValue()).doubleValue()));
    }

    @Override
    protected ObservableValue<Double> getObservableValue() {
      return getEditor().numberProperty();
    }

    @Override
    public void setValue(Double value) {
      getEditor().setNumber(value);
    }

  }

  private static class IntegerPropertyEditor extends AbstractEditor<Integer, IntegerField> {

    IntegerPropertyEditor(Item item) {
      super(item, new IntegerField((Integer) item.getValue()));
    }

    @Override
    protected ObservableValue<Integer> getObservableValue() {
      return getEditor().numberProperty();
    }

    @Override
    public void setValue(Integer value) {
      getEditor().setNumber(value);
    }
  }


  private static class TextPropertyEditor extends AbstractEditor<String, TextField> {

    TextPropertyEditor(Item item) {
      super(item, new TextField((String) item.getValue()));
    }

    @Override
    protected ObservableValue<String> getObservableValue() {
      return getEditor().textProperty();
    }

    @Override
    public void setValue(String value) {
      getEditor().setText(value);
    }

  }

  private static class ToggleSwitchEditor extends AbstractEditor<Boolean, ToggleSwitch> {

    ToggleSwitchEditor(Item item) {
      super(item, new ToggleSwitch());
    }

    @Override
    protected ObservableValue<Boolean> getObservableValue() {
      return getEditor().selectedProperty();
    }

    @Override
    public void setValue(Boolean value) {
      getEditor().setSelected(value);
    }

  }

  private static class ThemePropertyEditor extends AbstractPropertyEditor<Theme, ComboBox<Theme>> {

    private static class ThemeStringConverter extends StringConverter<Theme> {

      @Override
      public String toString(Theme object) {
        return object.getName();
      }

      @Override
      public Theme fromString(String string) {
        return Themes.getDefault().forName(string);
      }
    }

    ThemePropertyEditor(PropertySheet.Item property) {
      super(property, new ComboBox<>());
      getEditor().setItems(Themes.getDefault().getThemes());
      getEditor().setConverter(new ThemeStringConverter());
    }

    @Override
    protected ObservableValue<Theme> getObservableValue() {
      return getEditor().getSelectionModel().selectedItemProperty();
    }

    @Override
    public void setValue(Theme value) {
      getEditor().getSelectionModel().select(value);
    }
  }

}
