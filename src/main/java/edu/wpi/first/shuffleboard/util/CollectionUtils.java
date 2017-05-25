package edu.wpi.first.shuffleboard.util;

import java.util.Iterator;
import java.util.List;

/**
 * Utility class for collections.
 */
public final class CollectionUtils {

  private CollectionUtils() {
  }

  /**
   * Gets the element at the given index in the given iterable, or null if the index
   * is out of bounds.
   */
  public static <T> T elementAt(Iterable<? extends T> iterable, int index) {
    if (iterable instanceof List) {
      return ((List<T>) iterable).get(index);
    }
    Iterator<? extends T> iterator = iterable.iterator();
    int count = 0;
    while (iterator.hasNext()) {
      T element = iterator.next();
      if (index == count++) {
        return element;
      }
    }
    return null;
  }

}
