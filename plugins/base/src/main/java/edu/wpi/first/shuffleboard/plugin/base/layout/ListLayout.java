package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.util.ListUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.LayoutBase;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import com.google.common.collect.ImmutableList;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

@ParametrizedController("ListLayout.fxml")
public class ListLayout extends LayoutBase {

  @FXML
  private StackPane root;
  @FXML
  private VBox container;

  private final ObservableList<Component> widgets = FXCollections.observableArrayList();
  private final Property<Side> labelPosition = new SimpleObjectProperty<>(this, "labelPosition", Side.BOTTOM);

  private Subscription retained; //NOPMD field due to GC

  private final WeakHashMap<Component, Pane> panes = new WeakHashMap<>();

  @FXML
  protected void initialize() {
    retained = EasyBind.listBind(container.getChildren(), EasyBind.map(widgets, this::paneFor));

    // Move labels and recalculate their sizes
    labelPosition.addListener((__, old, position) -> {
      List<BorderPane> panes = this.panes.values().stream()
          .map(p -> (BorderPane) p)
          .collect(Collectors.toList());
      panes.forEach(p -> {
        Node label = getNodeForSide(p, old);
        setLabelPos(position, p, label);
      });
      calculateLabelSizes(position, panes);
    });

    // Recalculate label sizes when adding or removing children
    widgets.addListener((InvalidationListener) __ -> {
      List<BorderPane> panes = this.panes.values().stream()
          .map(p -> (BorderPane) p)
          .collect(Collectors.toList());
      calculateLabelSizes(labelPosition.getValue(), panes);
    });
  }

  /**
   * Sets the widths of the labels to keep them vertically aligned, if the side is LEFT or RIGHT. Otherwise, labels
   * are left to size themselves.
   *
   * @param side  the side of the pane that labels are on
   * @param panes the panes containing the labels
   */
  private void calculateLabelSizes(Side side, Collection<BorderPane> panes) {
    List<EditableLabel> labels = panes.stream()
        .map(p -> (EditableLabel) p.lookup(".layout-label"))
        .collect(Collectors.toList());
    if (side == Side.LEFT || side == Side.RIGHT) {
      double maxWidth = labels.stream()
          .mapToDouble(Region::getWidth)
          .max()
          .orElse(0);
      if (maxWidth > 0) {
        labels.forEach(l -> l.setMinWidth(maxWidth));
      }
    } else {
      labels.forEach(l -> l.setMinWidth(-1));
    }
  }

  private void setLabelPos(Side position, BorderPane p, Node label) {
    switch (position) {
      case TOP:
        p.setTop(label);
        break;
      case LEFT:
        p.setLeft(label);
        break;
      case RIGHT:
        p.setRight(label);
        break;
      case BOTTOM:
        p.setBottom(label);
        break;
      default:
        throw new AssertionError("Unknown side: " + position);
    }
  }

  private static Node getNodeForSide(BorderPane pane, Side side) {
    switch (side) {
      case TOP:
        return getAndClear(pane.topProperty());
      case LEFT:
        return getAndClear(pane.leftProperty());
      case RIGHT:
        return getAndClear(pane.rightProperty());
      case BOTTOM:
        return getAndClear(pane.bottomProperty());
      default:
        throw new AssertionError("Unknown side: " + side);
    }
  }

  /**
   * Gets the value of a property before clearing it.
   *
   * @param property the property to get and clear
   * @param <T>      the type of values in the property
   *
   * @return the value of the property prior to being cleared
   */
  private static <T> T getAndClear(Property<T> property) {
    T value = property.getValue();
    property.setValue(null);
    return value;
  }

  private Pane paneFor(Component component) {
    if (panes.containsKey(component)) {
      return panes.get(component);
    }

    BorderPane pane = new BorderPane(component.getView());
    ActionList.registerSupplier(pane, () -> actionsForComponent(component));
    pane.getStyleClass().add("layout-stack");
    EditableLabel label = new EditableLabel(component.titleProperty());
    label.getStyleClass().add("layout-label");
    ((Label) label.lookup(".label")).setTextOverrun(OverrunStyle.LEADING_ELLIPSIS);
    BorderPane.setAlignment(label, Pos.TOP_LEFT);
    setLabelPos(labelPosition.getValue(), pane, label);

    panes.put(component, pane);
    return pane;
  }

  @Override
  protected ActionList actionsForComponent(Component component) {
    ActionList actions = baseActionsForComponent(component);
    int index = widgets.indexOf(component);

    if (index > 0) {
      actions.addAction("Move up", () -> {
        widgets.remove(index);
        widgets.add(index - 1, component);
      });
      actions.addAction("Send to top", () -> {
        widgets.remove(index);
        widgets.add(0, component);
      });
    }

    if (index < widgets.size() - 1) {
      actions.addAction("Move down", () -> {
        widgets.remove(index);
        widgets.add(index + 1, component);
      });
      actions.addAction("Send to bottom", () -> {
        widgets.remove(index);
        widgets.add(component);
      });
    }

    return actions;
  }

  @Override
  protected void addComponentToView(Component component) {
    widgets.add(component);
  }

  @Override
  protected void removeComponentFromView(Component component) {
    widgets.remove(component);
  }

  @Override
  protected void replaceInPlace(Component existing, Component replacement) {
    ListUtils.replaceIn(widgets)
        .replace(existing)
        .with(replacement);
  }

  @Override
  public List<? extends Property<?>> getProperties() {
    return ImmutableList.of(
        labelPosition
    );
  }

  @Override
  public Collection<Component> getChildren() {
    return widgets;
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public String getName() {
    return "List Layout";
  }

  @Override
  public void addChild(Component widget) {
    widgets.add(widget);
  }
}
