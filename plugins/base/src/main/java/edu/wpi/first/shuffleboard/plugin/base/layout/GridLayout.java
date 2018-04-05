package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.properties.SavePropertyFrom;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.util.ListUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.LayoutBase;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.css.PseudoClass;
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

  private final IntegerProperty numColumns = new SimpleIntegerProperty(this, "columns", 1);
  private final IntegerProperty numRows = new SimpleIntegerProperty(this, "rows", 1);
  private final Map<Component, ChildContainer> panes = new WeakHashMap<>();
  private final Pane highlight = new Pane();

  /**
   * A placeholder for use in the grid. Placeholders are invisible and are behind all other components, and therefore
   * do not modify the UI or UX. These are used to simplify the computations for mapping drag points to grid coordinates
   * and for forcing empty columns and rows to have size (without placeholders, they would have zero width or height
   * until child components are added).
   */
  private static final class Placeholder extends Pane {

    final int col;
    final int row;

    Placeholder(int col, int row) {
      this.col = col;
      this.row = row;
      GridPane.setColumnIndex(this, col);
      GridPane.setRowIndex(this, row);
      setVisible(false);
    }

    @Override
    public boolean equals(Object obj) {
      return obj instanceof Placeholder
          && ((Placeholder) obj).col == this.col
          && ((Placeholder) obj).row == this.row;
    }

    @Override
    public int hashCode() {
      return Objects.hash(col, row);
    }
  }

  @FXML
  private void initialize() {
    highlight.getStyleClass().add("grid-highlight");
    for (int col = 0; col < numColumns.get(); col++) {
      for (int row = 0; row < numRows.get(); row++) {
        grid.getChildren().add(0, new Placeholder(col, row));
      }
    }
    numColumns.addListener((__, prev, cur) -> {
      int oldNum = prev.intValue();
      int newNum = cur.intValue();
      if (newNum > oldNum) {
        for (int col = oldNum; col < newNum; col++) {
          for (int row = 0; row < numRows.get(); row++) {
            Placeholder placeholder = new Placeholder(col, row);
            if (nodesInCol(col).count() == 0) {
              placeholder.prefWidthProperty().bind(grid.widthProperty().divide(numColumns));
            }
            if (nodesInRow(row).count() == 0) {
              placeholder.prefHeightProperty().bind(grid.heightProperty().divide(numRows));
            }
            ListUtils.addIfNotPresent(grid.getChildren(), 0, placeholder);
          }
        }
      } else {
        grid.getChildren().stream()
            .filter(child -> child instanceof Placeholder)
            .filter(child -> GridPane.getColumnIndex(child) > newNum)
            .collect(Collectors.toList())
            .forEach(grid.getChildren()::remove);
      }
    });
    numRows.addListener((__, prev, cur) -> {
      int oldNum = prev.intValue();
      int newNum = cur.intValue();
      if (newNum > oldNum) {
        for (int row = oldNum; row < newNum; row++) {
          for (int col = 0; col < numColumns.get(); col++) {
            Placeholder placeholder = new Placeholder(col, row);
            if (nodesInCol(col).count() == 0) {
              placeholder.prefWidthProperty().bind(grid.widthProperty().divide(numColumns));
            }
            if (nodesInRow(row).count() == 0) {
              placeholder.prefHeightProperty().bind(grid.heightProperty().divide(numRows));
            }
            ListUtils.addIfNotPresent(grid.getChildren(), 0, placeholder);
          }
        }
      } else {
        grid.getChildren().stream()
            .filter(child -> child instanceof Placeholder)
            .filter(child -> GridPane.getRowIndex(child) > newNum)
            .collect(Collectors.toList())
            .forEach(grid.getChildren()::remove);
      }
    });
    grid.setOnDragEntered(e -> {
      ListUtils.addIfNotPresent(grid.getChildren(), highlight);
      GridPane.setRowIndex(highlight, 0);
      GridPane.setColumnIndex(highlight, 0);
    });

    grid.setOnDragOver(event -> {
      GridPoint point = pointAt(event.getX(), event.getY());
      if (point != null) {
        ListUtils.addIfNotPresent(grid.getChildren(), highlight);
        GridPane.setRowIndex(highlight, point.row);
        GridPane.setColumnIndex(highlight, point.col);
      } else {
        grid.getChildren().remove(highlight);
      }
    });

    grid.setOnDragExited(e -> grid.getChildren().remove(highlight));
  }

  @Override
  public void addChild(Component component, double x, double y) {
    addChild(component, pointAt(x, y));
  }

  /**
   * Adds a child component to a specific point in the grid. If the point is already occupied, the component will be
   * placed in the first open spot. If no spots are open, a new row will be created. The component will be added to
   * the leftmost column in the new row.
   *
   * @param component the component to add
   * @param point     the point to add the component at
   */
  public void addChild(Component component, GridPoint point) {
    grid.getChildren().remove(highlight);

    // Try to place the component in the requested spot, if it's available
    // Otherwise, place it in the first open spot, creating a new row to contain it if necessary
    boolean isPointOpen = grid.getChildren().stream()
        .filter(n -> !(n instanceof Placeholder))
        .map(GridPoint::fromNode)
        .noneMatch(p -> Objects.equals(point, p));

    if (point != null && isPointOpen) {
      ChildContainer pane = paneFor(component);
      point.applyTo(pane);
      grid.getChildren().add(pane);
    } else {
      addComponentToView(component);
    }
    ListUtils.addIfNotPresent(getChildren(), component);
  }

  /**
   * Converts local coordinates to a point in the grid. If the coordinates are outside the grid bounds, returns null.
   *
   * @param x the x-coordinate
   * @param y the y-coordinate
   */
  private GridPoint pointAt(double x, double y) {
    Optional<Node> over = grid.getChildren().stream()
        .filter(c -> c instanceof Placeholder)
        .filter(c -> c.contains(c.parentToLocal(x, y)))
        .findFirst();
    boolean overChild = grid.getChildren().stream()
        .filter(c -> c != highlight)
        .filter(c -> !(c instanceof Placeholder))
        .anyMatch(c -> c.contains(c.parentToLocal(x, y)));
    highlight.pseudoClassStateChanged(PseudoClass.getPseudoClass("colliding"), overChild);
    return over.map(GridPoint::fromNode)
        .orElse(null);
  }

  private boolean isManaged(Node node) {
    return GridPane.getColumnIndex(node) != null
        && GridPane.getRowIndex(node) != null
        && node != highlight;
  }

  /**
   * Gets a stream of all the managed (ie placeholder and component) nodes in the given column.
   *
   * @param col the column to get the nodes in
   */
  private Stream<Node> nodesInCol(int col) {
    return grid.getChildren().stream()
        .filter(this::isManaged)
        .filter(n -> GridPane.getColumnIndex(n) == col);
  }

  /**
   * Gets a stream of all the managed (ie placeholder and component) nodes in the given row.
   *
   * @param row the row to get the nodes in
   */
  private Stream<Node> nodesInRow(int row) {
    return grid.getChildren()
        .stream()
        .filter(this::isManaged)
        .filter(n -> GridPane.getRowIndex(n) == row);
  }

  /**
   * Checks if a point in the grid has a child component.
   *
   * @param col the column to check
   * @param row the row to check
   *
   * @return true if the point does not contain a child component, false if it does
   */
  private boolean isOpen(int col, int row) {
    return grid.getChildren().stream()
        .filter(this::isManaged)
        .filter(n -> !(n instanceof Placeholder))
        .noneMatch(n -> GridPane.getColumnIndex(n) == col && GridPane.getRowIndex(n) == row);
  }

  private ChildContainer paneFor(Component component) {
    if (panes.containsKey(component)) {
      return panes.get(component);
    }
    ChildContainer pane = new ChildContainer(component);
    pane.labelSideProperty().bindBidirectional(this.labelSideProperty());
    GridPane.setHgrow(pane, Priority.ALWAYS);
    GridPane.setVgrow(pane, Priority.ALWAYS);
    ActionList.registerSupplier(pane, () -> actionsForComponent(component));
    panes.put(component, pane);
    return pane;
  }

  @Override
  protected void addComponentToView(Component component) {
    Node pane = paneFor(component);

    // Find the first open spot
    boolean anyOpen = false;
    int lastRow = numRows.get();
    for (int col = 0; col < numColumns.get() && !anyOpen; col++) {
      for (int row = 0; row < lastRow; row++) {
        if (isOpen(col, row)) {
          // Reset placeholder sizes so the row/column uses the size of the new component
          // instead of the placeholder size
          nodesInCol(col)
              .flatMap(TypeUtils.castStream(Placeholder.class))
              .forEach(p -> p.prefWidthProperty().unbind());
          nodesInRow(row)
              .flatMap(TypeUtils.castStream(Placeholder.class))
              .forEach(p -> p.prefHeightProperty().unbind());
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
      numRows.set(lastRow + 1);
      GridPane.setRowIndex(pane, lastRow);
      GridPane.setColumnIndex(pane, 0);
    }
    grid.getChildren().add(pane);
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

  public List<ChildContainer> getContainers() {
    return grid.getChildren().stream()
        .flatMap(TypeUtils.castStream(ChildContainer.class))
        .collect(Collectors.toList());
  }

}
