package edu.wpi.first.shuffleboard.api.util;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Utility class for working with lists.
 */
public final class ListUtils {

  private ListUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Replaces the first instance of an element in a list with another.
   *
   * @param list the list to replace the item in
   * @param <T>  the type of elements in the list
   */
  public static <T> Replacement<T> replaceIn(List<T> list) {
    return new Replacement<>(list);
  }

  public static final class Replacement<T> {

    private enum Strategy {
      FIRST,
      ALL
    }

    private static final Predicate ALWAYS_FALSE = __ -> false;

    private final List<T> list;
    private Predicate<? super T> test = ALWAYS_FALSE;
    private Strategy strategy = Strategy.FIRST;

    Replacement(List<T> list) {
      this.list = list;
    }

    /**
     * Sets a predicate to use to test elements in the list. Any element matching the predicate will be removed
     * according to the replacement strategy.
     *
     * @param test the test for elements to replace
     *
     * @return this replacement
     */
    public Replacement<T> replace(Predicate<? super T> test) {
      Objects.requireNonNull(test, "test");
      this.test = test;
      return this;
    }

    /**
     * Sets the element to be replaced.
     *
     * @param value the element to be replaced
     *
     * @return this replacement
     */
    public Replacement<T> replace(T value) {
      return replace(t -> EqualityUtils.isEqual(value, t));
    }

    /**
     * Makes only the first instance of the target value be replaced. This is the default replacement strategy.
     *
     * @return this replacement
     *
     * @see #all()
     */
    public Replacement<T> first() {
      strategy = Strategy.FIRST;
      return this;
    }

    /**
     * Makes all instances of the target value be replaced.
     *
     * @return this replacement
     *
     * @see #first()
     */
    public Replacement<T> all() {
      strategy = Strategy.ALL;
      return this;
    }

    /**
     * Replaces the elements in the list with the replacement one.
     *
     * @param replacement the element to replace the existing one
     */
    public void with(T replacement) {
      with(() -> replacement);
    }

    /**
     * Replaces the elements in the list with values given by the supplier.
     *
     * @param replacementSupplier the supplier to use to get replacement values
     *
     * @throws IllegalStateException if no values were specified to be replaced with {@link #replace(T)}
     *                               or {@link #replace(Predicate)}
     */
    public void with(Supplier<? extends T> replacementSupplier) {
      Objects.requireNonNull(replacementSupplier, "replacementSupplier");
      if (test.equals(ALWAYS_FALSE)) {
        throw new IllegalStateException("No values were specified to be replaced");
      }
      for (int i = 0; i < list.size(); i++) {
        if (test.test(list.get(i))) {
          list.set(i, replacementSupplier.get());
          if (strategy == Strategy.FIRST) {
            return;
          }
        }
      }
    }

  }

}
