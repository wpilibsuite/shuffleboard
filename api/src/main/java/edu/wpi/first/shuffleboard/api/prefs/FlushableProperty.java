package edu.wpi.first.shuffleboard.api.prefs;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A {@link Property} that can be flushed.
 *
 * @param <T> the type of value in this item
 */
public class FlushableProperty<T> extends SimpleObjectProperty<T> {

  private final Property<T> property;

  /**
   * Create a new FlushableProperty.
   *
   * @param property The property to flush to
   */
  public FlushableProperty(Property<T> property) {
    super(property.getBean(), property.getName(), property.getValue());
    this.property = property;
  }

  public boolean isChanged() {
    return !property.getValue().equals(getValue());
  }

  public void flush() {
    property.setValue(getValue());
  }
}
