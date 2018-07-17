package edu.wpi.first.shuffleboard.app.components;

import edu.wpi.first.shuffleboard.api.tab.model.ComponentModel;
import edu.wpi.first.shuffleboard.api.tab.model.ParentModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabModel;
import edu.wpi.first.shuffleboard.api.tab.model.WidgetModel;
import edu.wpi.first.shuffleboard.api.util.Debouncer;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.ComponentContainer;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.time.Duration;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.property.Property;

/**
 * A dashboard tab that generates its own contents based on an external model.
 */
public class ProcedurallyDefinedTab extends DashboardTab {

  private final TabModel model;
  private boolean deferPopulation = false; // NOPMD

  private final Map<ComponentModel, Component> proceduralComponents = new WeakHashMap<>();
  private final Debouncer populateDebouncer =
      new Debouncer(() -> FxUtils.runOnFxThread(this::populate), Duration.ofMillis(50));

  public ProcedurallyDefinedTab(TabModel model) {
    super(model.getTitle());
    this.model = model;
    getStyleClass().add("procedurally-defined-tab");
  }

  @Override
  public boolean isAutoPopulate() {
    return false;
  }

  @Override
  public void setAutoPopulate(boolean autoPopulate) {
    // NOP
  }

  @Override
  Debouncer getPopulateDebouncer() {
    return populateDebouncer;
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
      populateDebouncer.run();
      return;
    }
    if (deferPopulation) {
      // Defer one last time; this method tends to trigger before row/column bindings on the widget pane
      // This makes sure the pane is properly sized before populating it
      deferPopulation = false;
      populateDebouncer.run();
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
        if (container instanceof WidgetPane) {
          // Set the size and position in the widget pane
          // Does not apply to layouts, since they do not necessarily support this behavior
          addToWidgetPane((WidgetPane) container, componentModel, component);
        } else {
          container.addComponent(component);
        }
        proceduralComponents.put(componentModel, component);
      }
      applySettings(component, componentModel.getProperties());
      if (componentModel instanceof ParentModel) {
        populateLayout((ParentModel) componentModel, (ComponentContainer) proceduralComponents.get(componentModel));
      }
    }
  }

  private void addToWidgetPane(WidgetPane widgetPane, ComponentModel componentModel, Component component) {
    GridPoint position = componentModel.getPreferredPosition();
    if (position != null) {
      TileSize size = componentModel.getPreferredSize();
      if (size != null) {
        widgetPane.addComponent(component, position, size);
      } else {
        widgetPane.addComponent(component, position);
      }
    } else {
      widgetPane.addComponent(component);
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
