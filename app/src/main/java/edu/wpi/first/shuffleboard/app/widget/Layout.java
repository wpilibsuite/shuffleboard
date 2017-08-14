package edu.wpi.first.shuffleboard.app.widget;

import edu.wpi.first.shuffleboard.api.widget.Viewable;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.util.Collection;
import java.util.stream.Stream;

public interface Layout extends Viewable {
  Collection<Viewable> getChildren();
  void addChild(Viewable widget);

  default Stream<Widget> allWidgets() {
    return getChildren().stream().flatMap(Viewable::allWidgets);
  }
}
