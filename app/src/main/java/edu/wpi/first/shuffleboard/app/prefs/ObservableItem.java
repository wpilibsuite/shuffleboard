package edu.wpi.first.shuffleboard.app.prefs;

import org.controlsfx.control.PropertySheet;

import java.util.Optional;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

/**
 * An implementation of {@link PropertySheet.Item} that is backed by a JavaFX property.
 *
 * @param <T> the type of value in this item
 */
public class ObservableItem<T> implements PropertySheet.Item {

  private final Class<T> type;
  private final Property<T> property;
  private final String category;
  private final String description;

  /**
   * Creates a new observable item from the given property and with the given category. The type
   * is inferred from the current value of the property.
   */
  @SuppressWarnings("unchecked")
  public ObservableItem(Property<T> property, String category) {
    this((Class<T>) property.getValue().getClass(), property, category, null);
  }

  private ObservableItem(Class<T> type, Property<T> property, String category, String description) {
    this.type = type;
    this.property = property;
    this.category = category;
    this.description = description;
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
    return description;
  }

  @Override
  public Object getValue() {
    return property.getValue();
  }

  @Override
  @SuppressWarnings("unchecked")
  public void setValue(Object value) {
    if (!type.isInstance(value)) {
      throw new IllegalArgumentException("'" + value + "' is not of type " + type.getName());
    }
    property.setValue((T) value);
  }

  @Override
  public Optional<ObservableValue<? extends Object>> getObservableValue() {
    return Optional.of(property);
  }

}
