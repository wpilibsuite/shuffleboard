package edu.wpi.first.shuffleboard.app.prefs;

import javafx.beans.property.SimpleObjectProperty;
import org.controlsfx.control.PropertySheet;

import java.util.Optional;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

/**
 * An implementation of {@link PropertySheet.Item} that can be flushed to a JavaFX property.
 *
 * @param <T> the type of value in this item
 */
public class FlushableItem<T> implements PropertySheet.Item {

  private final Class type;
  private final Property<T> property;
  private final String category;

  private Property<T> value;

  public FlushableItem(Property<T> property, String category) {
    this.type = property.getValue().getClass();
    this.property = property;
    this.category = category;

    value = new SimpleObjectProperty(property.getValue());
  }

  @Override
  public Class<?> getType() {
    return type;
  }

  @Override
  public String getCategory() {
    return category;
  }

  @Override
  public String getName() {
    return property.getName();
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Object getValue() {
    return value.getValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setValue(Object value) {
    if (!type.isInstance(value)) {
      throw new IllegalArgumentException("'" + value + "' is not of type " + type.getName());
    }

    this.value.setValue((T) value);
  }

  @Override
  public Optional<ObservableValue<?>> getObservableValue() {
    return Optional.of(value);
  }

  public boolean isChanged() {
    return !property.getValue().equals(value.getValue());
  }

  public void flush() {
    System.out.println("Property Updated!");
    property.setValue(value.getValue());
  }

}
