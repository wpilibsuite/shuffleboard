package edu.wpi.first.shuffleboard.api.widget;

import java.util.stream.Stream;

/**
 * A common interface for objects that can contain components.
 */
public interface ComponentContainer {

  /**
   * Adds a component to this container. Where and how the component is laid out is up to the implementation.
   *
   * @param component the component to add
   */
  void addComponent(Component component);

  /**
   * Gets a stream of all the first-level components in this container.
   */
  Stream<Component> components();

}
