package edu.wpi.first.shuffleboard.api.properties;

import java.util.concurrent.atomic.AtomicReference;

import javafx.beans.InvalidationListener;
import javafx.beans.property.ObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * A thread-safe implementation of a property. Any changes to the value will execute on the JavaFX
 * application thread.
 */
@SuppressWarnings("OverloadMethodsDeclarationOrder")
public class AsyncProperty<T> extends ObjectProperty<T> implements AtomicProperty<T> {

  private final AtomicReference<T> holder = new AtomicReference<>(null);

  private final AtomicPropertyListenerDelegate<T> delegate;
  private final Object bean;
  private final String name;

  public AsyncProperty() {
    this(null, null, null);
  }

  public AsyncProperty(T initialValue) {
    this(null, null, initialValue);
  }

  public AsyncProperty(Object bean, String name) {
    this(bean, name, null);
  }

  @SuppressWarnings("JavadocMethod")
  public AsyncProperty(Object bean, String name, T initialValue) {
    this.bean = bean;
    this.name = name;
    holder.set(initialValue);
    delegate = new AtomicPropertyListenerDelegate<>(this);
  }

  @Override
  public void bind(ObservableValue<? extends T> observable) {
    delegate.bind(observable);
  }

  @Override
  public void unbind() {
    delegate.unbind();
  }

  @Override
  public boolean isBound() {
    return delegate.isBound();
  }

  @Override
  public Object getBean() {
    return bean;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public T get() {
    return holder.get();
  }

  @Override
  public void set(T newValue) {
    T oldValue = holder.get();
    holder.set(newValue);
    delegate.invalidated(oldValue, newValue);
  }

  @Override
  public void setValue(T v) {
    set(v);
  }

  @Override
  public void addImmediateListener(ImmediateChangeListener<? super T> listener) {
    delegate.addImmediateListener(listener);
  }

  @Override
  public void removeImmediateListener(ImmediateChangeListener<? super T> listener) {
    delegate.removeImmediateListener(listener);
  }

  @Override
  public void addListener(ChangeListener<? super T> listener) {
    delegate.addChangeListener(listener);
  }

  @Override
  public void removeListener(ChangeListener<? super T> listener) {
    delegate.removeChangeListener(listener);
  }

  @Override
  public void removeListener(InvalidationListener listener) {
    delegate.removeInvalidationListener(listener);
  }

  @Override
  public void addListener(InvalidationListener listener) {
    delegate.addInvalidationListener(listener);
  }

}
