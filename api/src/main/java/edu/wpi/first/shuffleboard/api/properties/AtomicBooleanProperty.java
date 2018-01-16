package edu.wpi.first.shuffleboard.api.properties;

import java.util.concurrent.atomic.AtomicBoolean;

import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

/**
 * An implementation of {@link BooleanProperty} that makes all reads and writes atomic.
 */
@SuppressWarnings("OverloadMethodsDeclarationOrder")
public class AtomicBooleanProperty extends BooleanProperty implements AtomicProperty<Boolean> {

  private final AtomicBoolean holder = new AtomicBoolean(false);
  private final String name;
  private final Object bean;
  
  private final AtomicPropertyListenerDelegate<Boolean> listenerDelegate;

  public AtomicBooleanProperty(boolean initialValue) {
    this(null, null, initialValue);
  }

  public AtomicBooleanProperty(String name, boolean initialValue) {
    this(null, name, initialValue);
  }

  @SuppressWarnings("JavadocMethod")
  public AtomicBooleanProperty(Object bean, String name, boolean initialValue) {
    this.bean = bean;
    this.name = name;
    holder.set(initialValue);
    listenerDelegate = new AtomicPropertyListenerDelegate<>(this);
  }

  @Override
  public void bind(ObservableValue<? extends Boolean> observable) {
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
  public boolean get() {
    return holder.get();
  }

  @Override
  public void set(boolean value) {
    boolean oldValue = holder.get();
    holder.set(value);
    listenerDelegate.invalidated(oldValue, value);
  }

  @Override
  public void setValue(Boolean v) {
    set(v != null && v);
  }

  @Override
  public void addImmediateListener(ImmediateChangeListener<? super Boolean> listener) {
    listenerDelegate.addImmediateListener(listener);
  }

  @Override
  public void removeImmediateListener(ImmediateChangeListener<? super Boolean> listener) {
    listenerDelegate.removeImmediateListener(listener);
  }

  @Override
  public void addListener(ChangeListener<? super Boolean> listener) {
    listenerDelegate.addChangeListener(listener);
  }

  @Override
  public void removeListener(ChangeListener<? super Boolean> listener) {
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
