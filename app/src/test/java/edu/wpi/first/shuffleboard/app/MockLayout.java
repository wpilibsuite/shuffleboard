package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public final class MockLayout implements Layout {

  private final List<Component> children = new ArrayList<>();
  private final Pane view = new VBox();

  @Override
  public Collection<Component> getChildren() {
    return children;
  }

  @Override
  public void addChild(Component widget) {
    children.add(widget);
    view.getChildren().add(new Label(widget.getName() + " - " + children.size()));
  }

  @Override
  public void removeChild(Component component) {
    children.remove(component);
  }

  @Override
  public Pane getView() {
    return view;
  }

  @Override
  public Property<String> titleProperty() {
    return new SimpleStringProperty();
  }

  @Override
  public String getName() {
    return "Mock Layout";
  }

  @Override
  public List<Group> getSettings() {
    return Collections.emptyList();
  }
}
