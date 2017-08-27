package edu.wpi.first.shuffleboard.api.components;

import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.util.Collection;
import java.util.stream.Stream;

public interface Layout extends Component {
  Collection<Component> getChildren();
  void addChild(Component widget);

  default Stream<Widget> allWidgets() {
    return getChildren().stream().flatMap(Component::allWidgets);
  }
}
