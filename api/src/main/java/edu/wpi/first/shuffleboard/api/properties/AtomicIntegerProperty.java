package edu.wpi.first.shuffleboard.api.properties;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.beans.InvalidationListener;
import javafx.beans.property.IntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * An implementation of {@link IntegerProperty} that makes all reads and writes atomic.
 */
@SuppressWarnings("OverloadMethodsDeclarationOrder")
public class AtomicIntegerProperty extends IntegerProperty implements AtomicProperty<Number> {

  private final AtomicInteger holder = new AtomicInteger(0);
  private final String name;
  private final Object bean;

  private final AtomicPropertyListenerDelegate<Number> listenerDelegate;

  public AtomicIntegerProperty() {
    this(null, null, 0);
  }

  public AtomicIntegerProperty(int initialValue) {
    this(null, null, initialValue);
  }

  public AtomicIntegerProperty(String name, int initialValue) {
    this(null, name, initialValue);
  }

  @SuppressWarnings("JavadocMethod")
  public AtomicIntegerProperty(Object bean, String name, int initialValue) {
    this.bean = bean;
    this.name = name;
    holder.set(initialValue);
    listenerDelegate = new AtomicPropertyListenerDelegate<>(this);
  }

  @Override
  public void bind(ObservableValue<? extends Number> observable) {
    listenerDelegate.bind(observable);
  }

  @Override
  public void unbind() {
    listenerDelegate.unbind();
  }

  @Override
  public boolean isBound() {
    return listenerDelegate.isBound();
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
  public int get() {
    return holder.get();
  }

  @Override
  public void set(int value) {
    int oldValue = holder.get();
    holder.set(value);
    listenerDelegate.invalidated(oldValue, value);
  }

  @Override
  public void setValue(Number v) {
    set(v == null ? 0 : v.intValue());
  }

  @Override
  public void addImmediateListener(ImmediateChangeListener<? super Number> listener) {
    listenerDelegate.addImmediateListener(listener);
  }

  @Override
  public void removeImmediateListener(ImmediateChangeListener<? super Number> listener) {
    listenerDelegate.removeImmediateListener(listener);
  }

  @Override
  public void addListener(ChangeListener<? super Number> listener) {
    listenerDelegate.addChangeListener(listener);
  }

  @Override
  public void removeListener(ChangeListener<? super Number> listener) {
    listenerDelegate.removeChangeListener(listener);
  }

  @Override
  public void addListener(InvalidationListener listener) {
    listenerDelegate.addInvalidationListener(listener);
  }

  @Override
  public void removeListener(InvalidationListener listener) {
    listenerDelegate.removeInvalidationListener(listener);
  }

}
