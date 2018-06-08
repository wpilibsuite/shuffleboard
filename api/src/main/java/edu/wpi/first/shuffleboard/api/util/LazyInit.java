package edu.wpi.first.shuffleboard.api.util;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * A holder class for lazily initialized values. This is useful for variables that may be constructed after its owner,
 * or when they may not necessarily be used at all during its owners lifecycle, and when it is expensive to instantiate.
 *
 * <p>Example use:
 * <pre>{@code
 * private final LazyInit<Foo> foo = LazyInit.of(() -> expensiveFooConstruction());
 *
 * void useFoo() {
 *   Foo foo = this.foo.get();
 *   // Do thing with foo
 * }
 * }</pre></p>
 *
 * @param <T> the type of the value to be initialized
 */
public final class LazyInit<T> implements Supplier<T> {

  private volatile boolean initialized = false;
  private T value = null;
  private final Callable<? extends T> initializer;

  /**
   * Creates a new lazy initializer.
   *
   * @param initializer the function to use to initialize the value when it is first accessed
   */
  public LazyInit(Callable<? extends T> initializer) {
    this.initializer = Objects.requireNonNull(initializer, "Initializer cannot be null");
  }

  /**
   * Creates a new lazy initializer.
   *
   * @param initializer the function to use to initialize the value when it is first accessed
   * @param <T>         the type of the value to be initialized
   *
   * @return a new lazy initializer
   */
  public static <T> LazyInit<T> of(Callable<? extends T> initializer) {
    return new LazyInit<>(initializer);
  }

  /**
   * Gets the value, initializing it if necessary. If the initializer throws an exception, further calls to this method
   * will attempt to initialize.
   *
   * @return the value
   *
   * @throws RuntimeException if initialization failed
   */
  @Override
  public T get() {
    if (!initialized) {
      try {
        value = initializer.call();
      } catch (Exception e) {
        throw new RuntimeException("Could not initialize", e);
      }
      initialized = true;
    }
    return value;
  }

}
