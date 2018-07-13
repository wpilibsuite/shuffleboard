package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.tab.model.ComponentModel;
import edu.wpi.first.shuffleboard.api.tab.model.ParentModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabModel;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.ComponentContainer;
import edu.wpi.first.shuffleboard.api.widget.Components;

import java.util.Map;
import java.util.WeakHashMap;

import javafx.application.Platform;

/**
 * A dashboard tab that generates its own contents based on an external model.
 */
public class ProcedurallyDefinedTab extends DashboardTab {

  private final TabModel model;
  private boolean deferPopulation = false; // NOPMD

  private final Map<ComponentModel, Component> proceduralComponents = new WeakHashMap<>();

  public ProcedurallyDefinedTab(TabModel model) {
    super(model.getTitle());
    this.model = model;
  }

  public void populate() {
    if (getTabPane() == null) {
      // No longer in the scene; bail
      return;
    }
    if (getWidgetPane().getScene() == null || getWidgetPane().getParent() == null) {
      // Defer until the pane is visible and is laid out in the scene
      deferPopulation = true;
      Platform.runLater(this::populate);
      return;
    }
    if (deferPopulation) {
      // Defer one last time; this method tends to trigger before row/column bindings on the widget pane
      // This makes sure the pane is properly sized before populating it
      deferPopulation = false;
      Platform.runLater(this::populate);
      return;
    }
    populateLayout(model, getWidgetPane());
  }

  private void populateLayout(ParentModel parent, ComponentContainer container) {
    for (ComponentModel componentModel : parent.getChildren().values()) {
      Component component = proceduralComponents.get(componentModel);
      if (component == null) {
        component = componentFor(componentModel);
        component.setTitle(componentModel.getTitle());
        proceduralComponents.put(componentModel, component);
        container.addComponent(component);
      }
      if (componentModel instanceof ParentModel) {
        populateLayout((ParentModel) componentModel, (ComponentContainer) proceduralComponents.get(componentModel));
      }
    }
  }

  private Component componentFor(ComponentModel model) {
    System.out.println("Getting component for " + model.getPath());
    return Components.getDefault().createComponent(model.getDisplayType())
        .orElseThrow(() -> new IllegalStateException("No available component for " + model.getDisplayType()));
  }

}
