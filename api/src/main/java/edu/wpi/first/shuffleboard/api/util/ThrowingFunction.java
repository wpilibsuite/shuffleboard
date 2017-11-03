package edu.wpi.first.shuffleboard.api.util;

/**
 * A version of {@link java.util.function.Function} that may throw a checked exception.
 *
 * @param <I> the input type of the function
 * @param <O> the output type of the function
 * @param <X> the type of throwable that the function may throw
 */
@FunctionalInterface
public interface ThrowingFunction<I, O, X extends Throwable> {

  O apply(I input) throws X;

}
