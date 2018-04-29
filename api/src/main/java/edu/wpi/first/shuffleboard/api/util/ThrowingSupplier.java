package edu.wpi.first.shuffleboard.api.util;

/**
 * An analogue to {@link java.util.function.Supplier} that may also throw a checked exception.
 *
 * @param <T> the type of results supplied by this supplier
 * @param <X> the type of exception thrown by {@link #get()}
 */
@FunctionalInterface
public interface ThrowingSupplier<T, X extends Exception> {

  /**
   * Gets a result.
   *
   * @return a result
   *
   * @throws X
   */
  T get() throws X;

}
