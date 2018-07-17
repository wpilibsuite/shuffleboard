package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.tab.model.ComponentModel;
import edu.wpi.first.shuffleboard.api.tab.model.ParentModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabModel;
import edu.wpi.first.shuffleboard.api.tab.model.WidgetModel;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.ComponentContainer;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.util.Map;
import java.util.WeakHashMap;

import javafx.application.Platform;
import javafx.beans.property.Property;

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

  @Override
  public boolean isAutoPopulate() {
    return false;
  }

  @Override
  public void setAutoPopulate(boolean autoPopulate) {
    // NOP
  }

  public void populate() {
    if (getTabPane() == null) {
      // No longer in the scene; bail
      return;
    }
    if (getWidgetPane().getScene() != null && !getWidgetPane().getScene().getWindow().isShowing()) {
      // Shuffleboard is closing; bail
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
        if (componentModel instanceof WidgetModel) {
          ((Widget) component).addSource(((WidgetModel) componentModel).getDataSource());
        }
        container.addComponent(component);
        proceduralComponents.put(componentModel, component);
      }
      applySettings(component, componentModel.getProperties());
      if (componentModel instanceof ParentModel) {
        populateLayout((ParentModel) componentModel, (ComponentContainer) proceduralComponents.get(componentModel));
      }
    }
  }

  private Component componentFor(ComponentModel model) {
    return Components.getDefault().createComponent(model.getDisplayType())
        .orElseThrow(() -> new IllegalStateException("No available component for " + model.getDisplayType()));
  }

  private void applySettings(Component component, Map<String, Object> properties) {
    properties.forEach((name, value) -> {
      component.getSettings().stream()
          .map(g -> g.getSettings())
          .flatMap(l -> l.stream())
          .forEach(s -> {
            if (s.getName().equalsIgnoreCase(name)) {
              ((Property) s.getProperty()).setValue(value);
            }
          });
    });
  }

}
