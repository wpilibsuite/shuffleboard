package edu.wpi.first.shuffleboard.plugin.base.widget;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.DoubleStream;

/**
 * An arraylist of primitive doubles. This class behaves much like {@link java.util.ArrayList}.
 *
 * <p>The <tt>size</tt>, <tt>isEmpty</tt>, <tt>get</tt>, and
 * <tt>iterator</tt> operations run in constant
 * time.  The <tt>add</tt> operation runs in <i>amortized constant time</i>,
 * that is, adding n elements requires O(n) time.  All of the other operations
 * run in linear time (roughly speaking).  The constant factor is low compared
 * to that for the <tt>LinkedList</tt> implementation.
 */
public class PrimitiveDoubleArrayList implements Iterable<Double> {

  private static final int INITIAL_SIZE = 16;

  private double[] array = new double[INITIAL_SIZE];
  private int size = 0;

  /**
   * Creates a new array list with space for 16 initial values.
   */
  public PrimitiveDoubleArrayList() {
    this(INITIAL_SIZE);
  }

  /**
   * Creates a new array list with a given size.
   *
   * @param initialSize the initial number of values the list should contain before needing to be resized
   */
  public PrimitiveDoubleArrayList(int initialSize) {
    if (initialSize <= 0) {
      throw new IllegalArgumentException("Initial size must be at least 1, was given: " + initialSize);
    }
    array = new double[initialSize];
  }

  /**
   * Adds a value to the end of this list.
   *
   * @param value the value to add
   */
  public void add(double value) {
    ensureCapacity(size + 1);
    array[size] = value;
    size++;
  }

  /**
   * Gets the value at the given index.
   *
   * @param index the index to get the value at
   *
   * @return the value at the given index
   *
   * @throws IndexOutOfBoundsException if <tt>index</tt> is negative or greater than the upper index
   */
  public double get(int index) {
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("Index must be in [0," + size + "), but was " + index);
    }
    return array[index];
  }

  /**
   * Removes excess entries from the backing array.
   */
  public void trimToSize() {
    if (size == array.length) {
      return;
    }
    double[] trimmed = new double[size];
    System.arraycopy(array, 0, trimmed, 0, size);
    array = trimmed;
  }

  /**
   * Gets the number of elements in this list.
   *
   * @return the number of elements in this list
   */
  public int size() {
    return size;
  }

  /**
   * Checks if this list contains any elements.
   *
   * @return <tt>false</tt> if this list contains anything, <tt>true</tt> if this list contains at least one element
   */
  public boolean isEmpty() {
    return size == 0;
  }

  /**
   * Removes all elements from this list.
   */
  public void clear() {
    array = new double[INITIAL_SIZE];
    size = 0;
  }

  /**
   * Creates a stream of all the elements in this list.
   */
  public DoubleStream stream() {
    if (size == 0) {
      return DoubleStream.empty();
    }
    return Arrays.stream(array, 0, size);
  }

  /**
   * Ensures that this list can contain at least <tt>capacity</tt> number of elements. This will grow the backing array
   * if needed.
   *
   * @param capacity the desired capacity of the list
   */
  private void ensureCapacity(int capacity) {
    if (array.length < capacity) {
      double[] bigger;
      if (array.length > Integer.MAX_VALUE / 2) {
        bigger = new double[Integer.MAX_VALUE];
      } else {
        bigger = new double[array.length * 2];
      }
      System.arraycopy(array, 0, bigger, 0, array.length);
      array = bigger;
    }
  }

  @Override
  public Iterator<Double> iterator() {
    return new Iterator<Double>() {
      private int index = 0;

      @Override
      public boolean hasNext() {
        return index < size();
      }

      @Override
      public Double next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return get(index++);
      }
    };
  }

  @Override
  public void forEach(Consumer<? super Double> action) {
    for (int i = 0; i < size; i++) {
      action.accept(array[i]);
    }
  }

  /**
   * Creates a new primitive <tt>double</tt> array containing all the values in this list.
   *
   * @return a new <tt>double</tt> array
   */
  public double[] toArray() {
    double[] arr = new double[size];
    if (size > 0) {
      System.arraycopy(array, 0, arr, 0, size);
    }
    return arr;
  }

}
