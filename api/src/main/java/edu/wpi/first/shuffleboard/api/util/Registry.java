package edu.wpi.first.shuffleboard.api.util;

import java.util.Collection;
import java.util.Objects;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Common superclass for registry classes.
 *
 * @param <T> the type of items that get registered
 */
public abstract class Registry<T> {

  private final ObservableList<T> items = FXCollections.observableArrayList();
  private final ObservableList<T> itemsUnmodifiable = FXCollections.unmodifiableObservableList(items);

  /**
   * Registers an item with this registry. Items are kept track of by hard references, meaning that any items registered
   * will <i>not</i> be garbage collected until they are {@link #unregister unregistered}.
   *
   * @param item the item to register
   *
   * @throws IllegalArgumentException if the item has already been registered
   * @throws NullPointerException     if the item is null
   * @implSpec implementations <i>must</i> call the protected method {@link #addItem} at the end of this method
   */
  public abstract void register(T item);

  /**
   * Registers an item only if it is not already registered.
   *
   * @param item the item to register
   *
   * @return true if the item was registered, false if not
   */
  public final boolean registerIfAbsent(T item) {
    if (isRegistered(item)) {
      return false;
    }
    register(item);
    return true;
  }

  /**
   * Unregisters an item from this registry.
   *
   * @param item the item to unregister
   *
   * @implSpec implementations <i>must</i> call the protected method {@link #removeItem} at the end of this method
   */
  public abstract void unregister(T item);

  /**
   * Adds an item to the list of registered items. This should <i>always</i> be called at the end of {@link #register}.
   */
  protected final void addItem(T item) {
    items.add(item);
  }

  /**
   * Removes an item from the list of registered items. This should <i>always</i> be called at the end of
   * {@link #unregister}.
   */
  protected final void removeItem(T item) {
    items.remove(item);
  }

  /**
   * Checks if the given item has been registered with this registry.
   */
  public final boolean isRegistered(T item) {
    return items.contains(item);
  }

  /**
   * Registers many items at once. This is equivalent to
   * <pre><code>
   *   for (T item : items) {
   *     register(item);
   *   }
   * </code></pre>
   *
   * @param items the items to register
   *
   * @throws IllegalArgumentException if any of the items is already registered
   */
  public final void registerAll(T... items) {
    Objects.requireNonNull(items, "items");
    for (T item : items) {
      register(item);
    }
  }

  /**
   * Registers many items at once.
   *
   * @param items the items to register
   *
   * @throws IllegalArgumentException if any of the items in the collection is already registered
   */
  public final void registerAll(Collection<? extends T> items) {
    Objects.requireNonNull(items, "items");
    items.forEach(this::register);
  }

  /**
   * Unregisters many items at once.
   *
   * @param items the items to unregister
   */
  public final void unregisterAll(T... items) {
    Objects.requireNonNull(items, "items");
    for (T item : items) {
      unregister(item);
    }
  }

  /**
   * Unregisters many items at once.
   *
   * @param items the items to unregister
   */
  public final void unregisterAll(Collection<? extends T> items) {
    Objects.requireNonNull(items, "items");
    items.forEach(this::unregister);
  }

  /**
   * Gets a <i>read-only</i> view of the list of registered items.
   */
  public final ObservableList<T> getItems() {
    return itemsUnmodifiable;
  }

}
