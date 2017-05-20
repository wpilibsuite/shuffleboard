package edu.wpi.first.shuffleboard.sources;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.util.HashMap;
import java.util.Map;

import static edu.wpi.first.shuffleboard.util.FxUtils.runOnFxThread;

/**
 * Abstract implementation of DataSource that defines properties for {@link #nameProperty()} , {@link #activeProperty()},
 * and {@link #dataProperty()} for subclasses.
 *
 * @param <T> the type of data this source provides
 */
public abstract class AbstractDataSource<T> implements DataSource<T> {

  protected final Property<String> name = new SimpleStringProperty(this, "name", "");
  protected final Property<Boolean> active = new SimpleBooleanProperty(this, "active", false);
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

  /**
   * A thread-safe implementation of a property. Any changes to the value or listeners will execute on the JavaFX
   * application thread.
   */
  static class ThreadSafeProperty<T> extends SimpleObjectProperty<T> {

    private final Map<ChangeListener<? super T>, ChangeListener<? super T>> wrappers = new HashMap<>();

    public ThreadSafeProperty() {
    }

    public ThreadSafeProperty(T initialValue) {
      super(initialValue);
    }

    public ThreadSafeProperty(Object bean, String name) {
      super(bean, name);
    }

    public ThreadSafeProperty(Object bean, String name, T initialValue) {
      super(bean, name, initialValue);
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
      if (wrappers.containsKey(listener)) {
        return;
      }
      wrappers.put(listener, ((observable, oldValue, newValue) -> runOnFxThread(() -> listener.changed(observable, oldValue, newValue))));
      super.addListener(wrappers.get(listener));
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
      super.removeListener(wrappers.get(listener));
    }

    @Override
    public void set(T newValue) {
      runOnFxThread(() -> super.set(newValue));
    }
  }

}
