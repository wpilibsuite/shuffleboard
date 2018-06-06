package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.dnd.DataFormats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

/**
 * A base class for layouts that provides helpful methods for interacting with components inside the layout.
 */
public abstract class LayoutBase implements Layout {

  private final List<Component> children = new ArrayList<>();
  private final StringProperty title = new SimpleStringProperty(this, "title", getName());
  private final Property<LabelPosition> labelPosition =
      new SimpleObjectProperty<>(this, "labelPosition", LabelPosition.BOTTOM);

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
  public void removeChild(Component component) {
    children.remove(component);
    removeComponentFromView(component);
  }

  @Override
  public Property<String> titleProperty() {
    return title;
  }

  /**
   * Gets the side on which labels for components should be displayed.
   */
  public final LabelPosition getLabelPosition() {
    return labelPosition.getValue();
  }

  public final Property<LabelPosition> labelPositionProperty() {
    return labelPosition;
  }

  /**
   * Sets the side on which labels for children should be displayed.
   */
  public final void setLabelPosition(LabelPosition labelPosition) {
    this.labelPosition.setValue(labelPosition);
  }

  /**
   * Creates all the menus needed for changing a widget to a different type.
   */
  protected final ActionList createChangeMenusForWidget(Widget widget) {
    ActionList actionList = ActionList.withName("Show as...");

    Stream<List<String>> componentNames;

    if (widget.getSources().isEmpty()) {
      // No sources; use all widgets compatible with all supported data types
      componentNames = widget.getDataTypes().stream()
          .map(Components.getDefault()::componentNamesForType);
    } else {
      componentNames = widget.getSources().stream()
          .map(Components.getDefault()::componentNamesForSource);
    }
    componentNames.flatMap(Collection::stream)
        .sorted()
        .distinct()
        .map(name -> createChangeAction(widget, name))
        .forEach(actionList::addAction);

    return actionList;
  }

  private ActionList.Action createChangeAction(Widget widget, String name) {
    boolean isSameWidget = name.equals(widget.getName());
    return ActionList.createAction(
        name,
        () -> change(widget, name),
        isSameWidget ? new Label("✓") : null
    );
  }

  private void change(Widget widget, String name) {
    boolean isSameWidget = name.equals(widget.getName());
    if (!isSameWidget) {
      Components.getDefault()
          .createWidget(name, widget.getSources())
          .ifPresent(replacement -> {
            replacement.setTitle(widget.getTitle());
            replaceInPlace(widget, replacement);
            getChildren().set(getChildren().indexOf(widget), replacement); // NOPMD - there's no enclosing class!
          });
    }
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
   * An enumeration of the possible positions for labels of components inside a layout.
   */
  public enum LabelPosition {
    /**
     * Labels will located above the component they label.
     */
    TOP,
    /**
     * Labels will located to the left of the component they label.
     */
    LEFT,
    /**
     * Labels will located to the right of the component they label.
     */
    RIGHT,
    /**
     * Labels will located below the component they label.
     */
    BOTTOM,
    /**
     * Labels will not be displayed.
     */
    HIDDEN
  }

  /**
   * A container for an individual component inside a layout. This contains an {@link EditableLabel} to display and edit
   * the title of the child. The position of this label can be configured with {@link #labelPositionProperty()}. API
   * consumers should usually bind this property to the layout's own {@link LayoutBase#labelPositionProperty() label
   * position}.
   */
  public static final class ChildContainer extends BorderPane {

    private final EditableLabel label = new EditableLabel();
    private final Property<LabelPosition> labelPosition =
        new SimpleObjectProperty<>(this, "labelPosition", LabelPosition.BOTTOM);
    private final Property<Component> child = new SimpleObjectProperty<>(this, "child", null);
    private final Layout layout;

    /**
     * Creates a new empty container.
     *
     * @param layout the layout containing this container
     */
    public ChildContainer(Layout layout) {
      this.layout = layout;
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
      labelPosition.addListener((__, oldSide, newSide) -> move(oldSide, newSide));
      set(getLabelSide(), label);
      setOnDragDetected(e -> {
        Component child = getChild();
        UUID parentId = Components.getDefault().uuidForComponent(this.layout);
        UUID childId = Components.getDefault().uuidForComponent(child);
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.put(DataFormats.tilelessComponent, new DataFormats.TilelessComponentData(parentId, childId));
        Dragboard dragboard = this.startDragAndDrop(TransferMode.MOVE);
        dragboard.setContent(clipboardContent);
        WritableImage preview =
            new WritableImage(
                (int) getBoundsInParent().getWidth(),
                (int) getBoundsInParent().getHeight()
            );
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        snapshot(parameters, preview);
        dragboard.setDragView(preview);
        e.consume();
      });
    }

    /**
     * Creates a container for the given component.
     *
     * @param child  the component to be contained
     * @param layout the layout containing this container
     */
    public ChildContainer(Component child, Layout layout) {
      this(layout);
      setChild(child);
    }

    private void move(LabelPosition oldPosition, LabelPosition newPosition) {
      set(oldPosition, null);
      set(newPosition, label);
    }

    private void set(LabelPosition position, Node node) {
      switch (position) {
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
        case HIDDEN:
          // Don't set it
          break;
        default:
          throw new AssertionError("Unknown position: " + position);
      }
    }

    public LabelPosition getLabelSide() {
      return labelPosition.getValue();
    }

    public Property<LabelPosition> labelPositionProperty() {
      return labelPosition;
    }

    public void setLabelSide(LabelPosition labelPosition) {
      this.labelPosition.setValue(labelPosition);
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
