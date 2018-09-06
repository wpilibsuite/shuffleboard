package edu.wpi.first.shuffleboard.api.util;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Utility class for working with lists.
 */
public final class ListUtils {

  private ListUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Gets the first index of an element matching a predicate.
   *
   * @param list      the list to search
   * @param predicate the predicate to use
   * @param <T>       the type of elements in the list
   *
   * @return the index of the first element to pass the predicate, or <tt>-1</tt> if no element matched
   *
   * @throws NullPointerException if either <tt>list</tt> or <tt>predicate</tt> is <tt>null</tt>
   */
  public static <T> int firstIndexOf(List<? extends T> list, Predicate<? super T> predicate) {
    Objects.requireNonNull(list, "list");
    Objects.requireNonNull(predicate, "predicate");
    for (int i = 0; i < list.size(); i++) {
      if (predicate.test(list.get(i))) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Adds an element to a list if the list does not already contain it.
   *
   * @param list    the list to add to
   * @param element the element to add
   * @param <T>     the type of values in the list
   *
   * @return true if the element was added to the list, false if not
   */
  public static <T> boolean addIfNotPresent(List<? super T> list, T element) {
    if (!list.contains(element)) {
      return list.add(element);
    }
    return false;
  }

  /**
   * Adds an element to a list if the list does not already contain it.
   *
   * @param list    the list to add to
   * @param index   the index in the list to add the element to
   * @param element the element to add
   * @param <T>     the type of values in the list
   *
   * @return true if the element was added, false if not
   */
  public static <T> boolean addIfNotPresent(List<? super T> list, int index, T element) {
    if (!list.contains(element)) {
      list.add(index, element);
      return true;
    }
    return false;
  }

  /**
   * Creates a new collector for immutable lists.
   *
   * <p>For example:
   * <pre>{@code
   * ImmutableList<T> list = values.stream()
   *   .filter(...)
   *   .map(...)
   *   .collect(toImmutableList());
   * }</pre>
   *
   * @param <T> the type of elements to collect
   */
  public static <T> Collector<T, ?, ImmutableList<T>> toImmutableList() {
    return Collector.<T, List<T>, ImmutableList<T>>of(
        ArrayList::new,
        List::add,
        (left, right) -> {
          left.addAll(right);
          return left;
        },
        ImmutableList::copyOf
    );
  }

  public static <T> Collector<T, ?, List<T>> joining(Supplier<? extends T> separator) {
    return joining(() -> null, separator, () -> null);
  }

  /**
   * Creates a collector for interleaving items with a constant separator item, along with prepending and appending
   * items to bookend the resulting list. If one of the suppliers gives a {@code null} result, no element will be added
   * for that part of the collection. For example, if {@code prefix} supplies {@code null}, then no element will be
   * prepended to the list, as opposed to prepending {@code null}.
   *
   * <p>Each supplier must return <i>consistent</i>, <i>equivalent</i> values. For example, {@code prefix} cannot
   * return {@code null} on one invocation and {@code "foo"} on another.
   */
  public static <T> Collector<T, ?, List<T>> joining(Supplier<? extends T> prefix,
                                                     Supplier<? extends T> separator,
                                                     Supplier<? extends T> suffix) {
    Objects.requireNonNull(prefix, "prefix");
    Objects.requireNonNull(separator, "separator");
    Objects.requireNonNull(suffix, "suffix");
    return new JoiningCollector<>(prefix, separator, suffix);
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

  private static class JoiningCollector<T> implements Collector<T, List<T>, List<T>> {

    private final Supplier<? extends T> prefix;
    private final Supplier<? extends T> separator;
    private final Supplier<? extends T> suffix;

    public JoiningCollector(Supplier<? extends T> prefix,
                            Supplier<? extends T> separator,
                            Supplier<? extends T> suffix) {
      this.prefix = prefix;
      this.separator = separator;
      this.suffix = suffix;
    }

    @Override
    public Supplier<List<T>> supplier() {
      return () -> {
        List<T> list = new ArrayList<>();
        T pre = prefix.get();
        if (pre != null) {
          list.add(pre);
        }
        return list;
      };
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
      return (list, element) -> {
        if (list.isEmpty() || (list.size() == 1 && prefix.get() != null)) {
          // If no elements: prefix is guaranteed null, so don't add a separator first
          // If 1 element:   if prefix is not null, then the only element is the prefix, so don't add a separator
          list.add(element);
        } else {
          T sep = separator.get();
          if (sep != null) {
            list.add(sep);
          }
          list.add(element);
        }
      };
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
      return (a, b) -> {
        a.addAll(b);
        return a;
      };
    }

    @Override
    public Function<List<T>, List<T>> finisher() {
      return list -> {
        T suf = suffix.get();
        if (suf != null) {
          list.add(suf);
        }
        return list;
      };
    }

    @Override
    public Set<Characteristics> characteristics() {
      // An ordered, serial collector with a nontrivial finisher function, so no characteristic applies
      return Collections.emptySet();
    }

  }
}
