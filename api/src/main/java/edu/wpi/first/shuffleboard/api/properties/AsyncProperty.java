package edu.wpi.first.shuffleboard.api.properties;

import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * A thread-safe implementation of a property. Any changes to the value will execute on the JavaFX
 * application thread.
 */
public class AsyncProperty<T> extends SimpleObjectProperty<T> {

  private final Map<ChangeListener<? super T>, ChangeListener<? super T>> wrappers = new WeakHashMap<>();

  public AsyncProperty() {
    super();
  }

  public AsyncProperty(T initialValue) {
    super(initialValue);
  }

  public AsyncProperty(Object bean, String name) {
    super(bean, name);
  }

  public AsyncProperty(Object bean, String name, T initialValue) {
    super(bean, name, initialValue);
  }

  @Override
  public void addListener(ChangeListener<? super T> listener) {
    if (wrappers.containsKey(listener)) {
      return;
    }
    wrappers.put(listener, (obs, prev, cur) -> AsyncUtils.runAsync(() -> listener.changed(obs, prev, cur)));
    super.addListener(wrappers.get(listener));
  }

  @Override
  public void removeListener(ChangeListener<? super T> listener) {
    super.removeListener(wrappers.get(listener));
    wrappers.remove(listener);
  }

  @Override
  public void set(T newValue) {
    AsyncUtils.runAsync(() -> super.set(newValue));
  }

}
