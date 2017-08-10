package edu.wpi.first.shuffleboard.app.widget;

import edu.wpi.first.shuffleboard.api.widget.Viewable;

public interface Layout extends Viewable {
  void addChild(Viewable widget);
}
