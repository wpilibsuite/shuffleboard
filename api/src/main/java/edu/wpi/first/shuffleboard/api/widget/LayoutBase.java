package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.components.ExtendedPropertySheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Side;
import javafx.scene.Node;
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
  private final Property<Side> labelSide = new SimpleObjectProperty<>(this, "labelSide", Side.BOTTOM);

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
  public List<Component> getChildren() {
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
   * Gets the side on which labels for components should be displayed.
   */
  public final Side getLabelSide() {
    return labelSide.getValue();
  }

  public final Property<Side> labelSideProperty() {
    return labelSide;
  }

  /**
   * Sets the side on which labels for children should be displayed.
   */
  public final void setLabelSide(Side labelSide) {
    Objects.requireNonNull(labelSide, "Label side cannot be null");
    this.labelSide.setValue(labelSide);
  }

  /**
   * Creates all the menus needed for changing a widget to a different type.
   */
  protected final ActionList createChangeMenusForWidget(Widget widget) {
    ActionList actionList = ActionList.withName("Show as...");

    widget.getSources().stream()
        .map(Components.getDefault()::componentNamesForSource)
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
                        children.set(children.indexOf(widget), replacement); // NOPMD - there is no inner/outer class!
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
    actions.addAction("Edit properties", () -> {
      ExtendedPropertySheet propertySheet = new ExtendedPropertySheet();
      propertySheet.getItems().add(new ExtendedPropertySheet.PropertyItem<>(component.titleProperty()));
      propertySheet.getItems().addAll(
          component.getProperties().stream()
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
    if (component instanceof Widget) {
      actions.addNested(createChangeMenusForWidget((Widget) component));
    }

    actions.addAction("Remove", () -> {
      removeComponentFromView(component);
      children.remove(component);
      if (component instanceof Sourced) {
        ((Sourced) component).removeAllSources();
      }
    });

    return actions;
  }

  /**
   * A container for an individual component inside a layout. This contains an {@link EditableLabel} to display and edit
   * the title of the child. The position of this label can be configured with {@link #labelSideProperty()}. API
   * consumers should usually bind this property to the layout's own {@link LayoutBase#labelSideProperty() label side}.
   */
  public static final class ChildContainer extends BorderPane {

    private final EditableLabel label = new EditableLabel();
    private final Property<Side> labelSide = new SimpleObjectProperty<>(this, "labelSide", Side.BOTTOM);
    private final Property<Component> child = new SimpleObjectProperty<>(this, "child", null);

    public ChildContainer() {
      setMaxWidth(Double.POSITIVE_INFINITY);
      getStyleClass().add("layout-stack");
      label.getStyleClass().add("layout-label");
      child.addListener((__, old, child) -> {
        if (old != null) {
          label.textProperty().unbindBidirectional(old.titleProperty());
        }
        if (child == null) {
          setCenter(null);
          set(getLabelSide(), null);
        } else {
          label.textProperty().bindBidirectional(child.titleProperty());
          setCenter(child.getView());
        }
      });
      labelSide.addListener((__, oldSide, newSide) -> move(oldSide, newSide));
      set(getLabelSide(), label);
    }

    public ChildContainer(Component child) {
      this();
      setChild(child);
    }

    private void move(Side oldSide, Side newSide) {
      set(oldSide, null);
      set(newSide, label);
    }

    private void set(Side side, Node node) {
      switch (side) {
        case TOP:
          setTop(node);
          break;
        case LEFT:
          setLeft(node);
          break;
        case RIGHT:
          setRight(node);
          break;
        case BOTTOM:
          setBottom(node);
          break;
        default:
          throw new AssertionError("Unknown side: " + side);
      }
    }

    public Side getLabelSide() {
      return labelSide.getValue();
    }

    public Property<Side> labelSideProperty() {
      return labelSide;
    }

    public void setLabelSide(Side labelSide) {
      this.labelSide.setValue(labelSide);
    }

    public Component getChild() {
      return child.getValue();
    }

    public Property<Component> childProperty() {
      return child;
    }

    public void setChild(Component child) {
      this.child.setValue(child);
    }
  }

}
