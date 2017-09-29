package edu.wpi.first.shuffleboard.api.widget;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * A Layout is a dashboard Component that holds other Components (i.e., widgets or other layouts) in a nested fashion.
 */
public interface Layout extends Component, ComponentContainer {

  Collection<Component> getChildren();

  void addChild(Component widget);

  @Override
  default void addComponent(Component component) {
    addChild(component);
  }

  @Override
  default Stream<Component> components() {
    return getChildren().stream();
  }

  default Stream<Widget> allWidgets() {
    return getChildren().stream().flatMap(Component::allWidgets);
  }
}
