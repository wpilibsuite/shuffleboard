package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.widget.AbstractWidget;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

class MockWidget extends AbstractWidget {

  private final Pane view = new Pane();

  public MockWidget() {
    view.getChildren().add(new Label(this.toString()));
  }

  @Override
  public Pane getView() {
    return view;
  }

  @Override
  public String getName() {
    return "Example";
  }
}
