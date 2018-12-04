package edu.wpi.first.shuffleboard.api.util.function;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A type of supplier that can have its output mapped to a different type.
 *
 * @param <T> the type of results supplied by this supplier
 */
@FunctionalInterface
public interface MappableSupplier<T> extends Supplier<T> {

  /**
   * Creates a new mappable supplier whose output corresponds, roughly, to {@code mappingFunction.apply(get())}.
   *
   * @param mappingFunction the function to use to map the output of this supplier to a different value
   * @param <U> the type of the mapped value
   *
   * @return a new mappable supplier
   */
  default <U> MappableSupplier<U> map(Function<? super T, ? extends U> mappingFunction) {
    return () -> mappingFunction.apply(get());
  }

}
