package edu.wpi.first.shuffleboard.api.properties;

import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.EqualityUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * A helper class that implements many of the listener and binding related methods for atomic properties. Since Java
 * does not support multiple inheritance, these could not have been implemented in an abstract superclass for atomic
 * properties that also need to subclass {@link javafx.beans.property.IntegerProperty IntegerProperty},
 * {@link javafx.beans.property.BooleanProperty BooleanProperty}, etc., as there are many helper functions in JavaFX
 * that expect {@code IntegerProperty} instead of {@code Property<Integer>}.
 */
public final class AtomicPropertyListenerDelegate<T> {

  private static final WeakReference EMPTY_REF = new WeakReference<>(null);
  private WeakReference<ObservableValue<? extends T>> bound = EMPTY_REF;
  private final List<InvalidationListener> invalidationListeners = new ArrayList<>();
  private final List<ImmediateChangeListener<? super T>> immediateListeners = new ArrayList<>();
  private final List<ChangeListener<? super T>> changeListeners = new ArrayList<>();

  private final AtomicProperty<T> atomicProperty;
  private final ChangeListener<? super T> bindingListener;

  public AtomicPropertyListenerDelegate(AtomicProperty<T> atomicProperty) {
    this.atomicProperty = atomicProperty;
    bindingListener = (__, oldValue, newValue) -> this.atomicProperty.setValue(newValue);
  }

  /**
   * Binds the atomic property to an observable value.
   *
   * @see Property#bind(ObservableValue)
   */
  public void bind(ObservableValue<? extends T> observableValue) {
    Objects.requireNonNull(observableValue, "Cannot bind to a null observable");
    bound = new WeakReference<>(observableValue);
    observableValue.addListener(bindingListener);
  }

  /**
   * Unbinds the atomic property.
   *
   * @see Property#unbind()
   */
  public void unbind() {
    if (isBound()) {
      ObservableValue<? extends T> oldBound = bound.get();
      if (oldBound != null) {
        oldBound.removeListener(bindingListener);
      }
      bound = EMPTY_REF;
    }
  }

  public boolean isBound() {
    return bound.get() != null;
  }

  /**
   * Fires all listeners when the value of the property changes. Invalidation listeners are scheduled first (but may not
   * <i>run</i> first if this is not called from the JavaFX application thread), then, if the value has changed,
   * the immediate listeners are called, and then the change listeners are scheduled to be called from the JavaFX
   * application thread.
   */
  public void invalidated(T oldValue, T newValue) {
    invalidationListeners.forEach(l -> AsyncUtils.runAsync(() -> l.invalidated(atomicProperty)));
    if (EqualityUtils.isDifferent(oldValue, newValue)) {
      immediateListeners.forEach(l -> l.changed(atomicProperty, oldValue, newValue));
      changeListeners.forEach(l -> AsyncUtils.runAsync(() -> l.changed(atomicProperty, oldValue, newValue)));
    }
  }

  public void addImmediateListener(ImmediateChangeListener<? super T> listener) {
    immediateListeners.add(listener);
  }

  public void removeImmediateListener(ImmediateChangeListener<? super T> listener) {
    immediateListeners.remove(listener);
  }

  public void addChangeListener(ChangeListener<? super T> listener) {
    changeListeners.add(listener);
  }

  public void removeChangeListener(ChangeListener<? super T> listener) {
    changeListeners.remove(listener);
  }

  public void addInvalidationListener(InvalidationListener listener) {
    invalidationListeners.add(listener);
  }

  public void removeInvalidationListener(InvalidationListener listener) {
    invalidationListeners.remove(listener);
  }

}
