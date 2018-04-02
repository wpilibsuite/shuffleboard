package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.components.ExtendedPropertySheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;

/**
 * A base class for layouts that provides helpful methods for interacting with components inside the layout.
 */
public abstract class LayoutBase implements Layout {

  private final List<Component> children = new ArrayList<>();
  private final StringProperty title = new SimpleStringProperty(this, "title", getName());

  /**
   * Adds a component to this layout's view.
   *
   * @param component the component to add
   */
  protected abstract void addComponentToView(Component component);

  /**
   * Removes a component from this layout's view.
   *
   * @param component the component to remove
   */
  protected abstract void removeComponentFromView(Component component);

  /**
   * Replaces a component with another one, keeping it in the same position as the original component.
   *
   * @param existing    the component to replace
   * @param replacement the component to replace it with
   */
  protected abstract void replaceInPlace(Component existing, Component replacement);

  @Override
  public Collection<Component> getChildren() {
    return children;
  }

  @Override
  public void addChild(Component component) {
    children.add(component);
    addComponentToView(component);
  }

  @Override
  public Property<String> titleProperty() {
    return title;
  }

  /**
   * Creates all the menus needed for changing a widget to a different type.
   */
  protected final ActionList createChangeMenusForWidget(Widget widget) {
    ActionList actionList = ActionList.withName("Show as...");

    widget.getDataTypes().stream()
        .map(Components.getDefault()::componentNamesForType)
        .flatMap(Collection::stream)
        .sorted()
        .distinct()
        .forEach(name -> {
          boolean isSameWidget = name.equals(widget.getName());
          actionList.addAction(
              name,
              isSameWidget ? new Label("✓") : null,
              () -> {
                if (!isSameWidget) {
                  Components.getDefault()
                      .createWidget(name, widget.getSources())
                      .ifPresent(replacement -> {
                        replacement.setTitle(widget.getTitle());
                        replaceInPlace(widget, replacement);
                      });
                }
              });
        });

    return actionList;
  }

  /**
   * Creates an action list for a component. Subclasses can override this, but should be sure to add additional actions
   * to the output of {@link #baseActionsForComponent(Component)} instead of creating a new action list from scratch.
   *
   * @param component the component to create the action list for
   */
  protected ActionList actionsForComponent(Component component) {
    return baseActionsForComponent(component);
  }

  /**
   * Creates the initial action list for a component. This list can be added to for layout-specific actions.
   *
   * @param component the component to create an action list for
   */
  protected final ActionList baseActionsForComponent(Component component) {
    ActionList actions = ActionList.withName(component.getTitle());
    if (component instanceof Widget) {
      Widget widget = (Widget) component;
      actions.addAction("Edit properties", () -> {
        ExtendedPropertySheet propertySheet = new ExtendedPropertySheet();
        propertySheet.getItems().add(new ExtendedPropertySheet.PropertyItem<>(widget.titleProperty()));
        propertySheet.getItems().addAll(
            widget.getProperties().stream()
                .map(ExtendedPropertySheet.PropertyItem::new)
                .collect(Collectors.toList()));
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit properties");
        dialog.getDialogPane().getStylesheets().setAll(getView().getScene().getRoot().getStylesheets());
        dialog.getDialogPane().setContent(new BorderPane(propertySheet));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.setResultConverter(button -> button);
        dialog.showAndWait();
      });
      actions.addNested(createChangeMenusForWidget(widget));
    }

    actions.addAction("Remove", () -> {
      removeComponentFromView(component);
      if (component instanceof Sourced) {
        ((Sourced) component).removeAllSources();
      }
    });

    return actions;
  }
}
