package edu.wpi.first.shuffleboard.api.properties;

import java.util.function.Predicate;

/**
 * A type of atomic property that has an input validator. Calling {@link #set} or {@link #setValue} with a value that
 * does not pass the validator will have no effect.
 */
public class AsyncValidatingProperty<T> extends AsyncProperty<T> {

  private final Predicate<? super T> validator;

  public AsyncValidatingProperty(Predicate<? super T> validator) {
    this.validator = validator;
  }

  public AsyncValidatingProperty(T initialValue, Predicate<? super T> validator) {
    super(initialValue);
    this.validator = validator;
  }

  public AsyncValidatingProperty(Object bean, String name, Predicate<? super T> validator) {
    super(bean, name);
    this.validator = validator;
  }

  public AsyncValidatingProperty(Object bean, String name, T initialValue, Predicate<? super T> validator) {
    super(bean, name, initialValue);
    this.validator = validator;
  }

  @Override
  public void set(T newValue) {
    if (validator.test(newValue)) {
      super.set(newValue);
    }
  }

  @Override
  public void setValue(T newValue) {
    if (validator.test(newValue)) {
      super.setValue(newValue);
    }
  }
}
