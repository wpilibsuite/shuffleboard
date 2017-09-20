package edu.wpi.first.shuffleboard.api.widget;

import java.util.Collection;
import java.util.stream.Stream;

/**
 * A Layout is a dashboard Component that holds other Components (i.e., widgets) in a nested fashion.
 */
public interface Layout extends Component {
  Collection<Component> getChildren();

  void addChild(Component widget);

  default Stream<Widget> allWidgets() {
    return getChildren().stream().flatMap(Component::allWidgets);
  }
}
