package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.LayoutBase;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

@ParametrizedController("GridLayout.fxml")
public class GridLayout extends LayoutBase {

  @FXML
  private Pane root;
  @FXML
  private GridPane grid;

  private final IntegerProperty numColumns = new SimpleIntegerProperty(this, "numColumns", 1);
  private final IntegerProperty numRows = new SimpleIntegerProperty(this, "numRows", 1);
  private final Map<Component, ChildContainer> panes = new WeakHashMap<>();

  @FXML
  private void initialize() {
    // Nothing to initialize
  }

  private void add(Component added) {
    Node pane = paneFor(added);

    // Find the first open spot
    boolean anyOpen = false;
    for (int col = 0; col < numColumns.get() && !anyOpen; col++) {
      for (int row = 0; row < numRows.get(); row++) {
        if (isOpen(col, row)) {
          GridPane.setColumnIndex(pane, col);
          GridPane.setRowIndex(pane, row);
          anyOpen = true;
          break;
        }
      }
    }

    // No open spots, create a new row to add the pane to
    if (!anyOpen) {
      // Add another row
      numRows.set(numRows.get() + 1);
      GridPane.setRowIndex(pane, numRows.get() - 1);
      GridPane.setColumnIndex(pane, 0);
    }
    grid.getChildren().add(pane);
  }

  private boolean isOpen(int col, int row) {
    return grid.getChildren().stream()
        .filter(n -> GridPane.getColumnIndex(n) != null)
        .noneMatch(n -> GridPane.getColumnIndex(n) == col && GridPane.getRowIndex(n) == row);
  }

  private Node paneFor(Component component) {
    if (panes.containsKey(component)) {
      return panes.get(component);
    }
    ChildContainer pane = new ChildContainer(component);
    pane.labelSideProperty().bindBidirectional(this.labelSideProperty());
    GridPane.setHgrow(pane, Priority.ALWAYS);
    GridPane.setRowIndex(pane, numRows.get() - 1);
    ActionList.registerSupplier(pane, () -> actionsForComponent(component));
    panes.put(component, pane);
    return pane;
  }

  @Override
  protected void addComponentToView(Component component) {
    add(component);
  }

  @Override
  protected void removeComponentFromView(Component component) {
    ChildContainer container = panes.remove(component);
    grid.getChildren().remove(container);
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
  public Pane getView() {
    return root;
  }

  @Override
  public List<Property<?>> getProperties() {
    return ImmutableList.of(
        numColumns,
        numRows,
        labelSideProperty()
    );
  }

  @Override
  public String getName() {
    return "Grid Layout";
  }
}
