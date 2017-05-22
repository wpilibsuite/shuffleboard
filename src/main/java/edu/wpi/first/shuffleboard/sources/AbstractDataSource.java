package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.properties.ThreadSafeProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

/**
 * Abstract implementation of DataSource that defines properties for {@link #nameProperty()} , {@link #activeProperty()},
 * and {@link #dataProperty()} for subclasses.
 *
 * @param <T> the type of data this source provides
 */
public abstract class AbstractDataSource<T> implements DataSource<T> {

  protected final Property<String> name = new SimpleStringProperty(this, "name", "");
  protected final Property<Boolean> active = new ThreadSafeProperty<>(this, "active", false);
  protected final Property<T> data = new ThreadSafeProperty<>(this, "data", null);

  @Override
  public ObservableValue<String> nameProperty() {
    return name;
  }

  @Override
  public ObservableValue<Boolean> activeProperty() {
    return active;
  }

  @Override
  public Property<T> dataProperty() {
    return data;
  }

  protected void setName(String name) {
    this.name.setValue(name);
  }

  protected void setActive(boolean active) {
    this.active.setValue(active);
  }

}
