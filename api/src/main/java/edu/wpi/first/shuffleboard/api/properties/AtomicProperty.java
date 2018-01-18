package edu.wpi.first.shuffleboard.api.properties;

import javafx.beans.property.Property;

/**
 * A type of property that makes all reads and writes atomic. This is intended for multithreaded use. Because the
 * JavaFX model assumes all changes are made on the JavaFX application thread, listeners and bindings may cause
 * subtle problems when not called from the JavaFX thread. Atomic properties have their values set immediately
 * from the calling thread, which then triggers all {@link ImmediateChangeListener immediate change listeers}
 * to also run from that thread; invalidation and normal change listeners are invoked asynchronously from the JavaFX
 * application thread. If a change is made on the JavaFX thread, then all listeners will run immediately.
 */
public interface AtomicProperty<T> extends Property<T> {

  /**
   * Adds a listener to be called immediately after the value changes, even when it changes from a thread other than
   * the JavaFX application thread.
   *
   * @see #addListener(javafx.beans.value.ChangeListener)
   */
  void addImmediateListener(ImmediateChangeListener<? super T> listener);

  /**
   * Removes an immediate listener from this property.
   *
   * @see #removeListener(javafx.beans.value.ChangeListener)
   */
  void removeImmediateListener(ImmediateChangeListener<? super T> listener);

}
