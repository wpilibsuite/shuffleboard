package edu.wpi.first.shuffleboard.api.properties;

import javafx.beans.value.ChangeListener;

/**
 * A change listener that is invoked <i>immediately</i> when an atomic property changes. When used as a normal change
 * listener, it has no special attributes; however, when added as an immediate listener to an atomic property, it will
 * always be called immediately from the same thread that changed the value, rather than the (potentially) asynchronous
 * call from the JavaFX thread made for normal change listeners.
 *
 * <p>This type of listener must <strong>not</strong> make any changes to normal JavaFX properties or call
 * methods on JavaFX nodes, as there is no guarantee that it will occur on the JavaFX thread.
 *
 * <p>This interface intentionally does not define any methods to maintain use as a functional interface.
 */
@FunctionalInterface
public interface ImmediateChangeListener<T> extends ChangeListener<T> {

}
