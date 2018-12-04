package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.util.ListUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.LayoutBase;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.WeakHashMap;

import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

@ParametrizedController("ListLayout.fxml")
public class ListLayout extends LayoutBase {

  @FXML
  private StackPane root;
  @FXML
  private VBox container;

  private final WeakHashMap<Component, ChildContainer> panes = new WeakHashMap<>();

  @FXML
  protected void initialize() {
    // Nothing to initialize
  }

  private ChildContainer paneFor(Component component) {
    if (panes.containsKey(component)) {
      return panes.get(component);
    }
    ChildContainer pane = new ChildContainer(component, this);
    pane.labelPositionProperty().bindBidirectional(this.labelPositionProperty());
    ActionList.registerSupplier(pane, () -> actionsForComponent(component));
    panes.put(component, pane);
    return pane;
  }

  @Override
  protected ActionList actionsForComponent(Component component) {
    ActionList actions = baseActionsForComponent(component);
    List<Component> components = getChildren();
    ObservableList<Node> views = container.getChildren();
    int index = ListUtils.firstIndexOf(views,
        n -> n instanceof ChildContainer && ((ChildContainer) n).getChild() == component);

    if (index > 0) {
      actions.addAction("Move up", () -> {
        views.add(index - 1, views.remove(index));
        components.add(index - 1, components.remove(index));
      });
      actions.addAction("Send to top", () -> {
        views.add(0, views.remove(index));
        components.add(0, components.remove(index));
      });
    }

    if (index < components.size() - 1) {
      actions.addAction("Move down", () -> {
        views.add(index + 1, views.remove(index));
        components.add(index + 1, components.remove(index));
      });
      actions.addAction("Send to bottom", () -> {
        views.add(views.remove(index));
        components.add(components.remove(index));
      });
    }

    return actions;
  }

  @Override
  protected void addComponentToView(Component component) {
    container.getChildren().add(paneFor(component));
  }

  @Override
  protected void removeComponentFromView(Component component) {
    ChildContainer container = panes.remove(component);
    this.container.getChildren().remove(container);
  }

  @Override
  protected void replaceInPlace(Component existing, Component replacement) {
    ChildContainer container = panes.remove(existing);
    container.setChild(replacement);

    // Update the actions for the pane - otherwise, it'll still have the same actions as the original component!
    ActionList.registerSupplier(container, () -> actionsForComponent(replacement));
    panes.put(replacement, container);
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Layout",
            Setting.of("Label position", labelPositionProperty(), LabelPosition.class)
        )
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public String getName() {
    return "List Layout";
  }
}
