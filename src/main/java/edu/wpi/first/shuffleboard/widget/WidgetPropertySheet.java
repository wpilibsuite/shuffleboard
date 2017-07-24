package edu.wpi.first.shuffleboard.widget;

import edu.wpi.first.shuffleboard.components.NumberField;

import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;

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
        if (Number.class.isAssignableFrom(item.getType())) {
          return new NumberPropertyEditor(item);
        }
        return super.call(item);
      }
    });
  }

  /**
   * An item backed by a JavaFX property.
   */
  private static class PropertyItem<T> implements Item {

    private final Property<T> property;
    private final String name;

    PropertyItem(Property<T> property) {
      this.property = property;
      this.name = camelCaseToSentence(property.getName());
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
        char c = chars[i];
        if (Character.isUpperCase(c)) {
          builder.append(' ').append(Character.toLowerCase(c));
        } else {
          builder.append(c);
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

  /**
   * A property editor for numbers. We use this instead of the one bundled with ControlsFX because
   * their implementation is bad.
   */
  private static class NumberPropertyEditor extends AbstractPropertyEditor<Double, NumberField> {

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

}
