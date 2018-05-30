package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.util.TypeUtils;

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
   * Removes a component from this container.
   *
   * @param component the component to remove
   */
  void removeComponent(Component component);

  /**
   * Gets a stream of all the first-level components in this container.
   */
  Stream<Component> components();

  /**
   * Gets a stream of all the components in this container.
   */
  default Stream<Component> allComponents() {
    return Stream.concat(
        components(),
        components()
            .flatMap(TypeUtils.castStream(ComponentContainer.class))
            .flatMap(ComponentContainer::allComponents)
    );
  }

}
