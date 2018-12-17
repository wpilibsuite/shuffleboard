package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
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
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

@SuppressWarnings("PMD.GodClass")
@ParametrizedController("GridLayout.fxml")
public final class GridLayout extends LayoutBase {

  @FXML
  private Pane root;
  @FXML
  private GridPane grid;

  @SuppressWarnings("PMD.LinguisticNaming") // Predicates prefixed with "is" makes PMD mad
  private static final Predicate<Node> isPlaceholder = n -> n instanceof Placeholder;

  private final IntegerProperty numColumns = new SimpleIntegerProperty(this, "columns", 3);
  private final IntegerProperty numRows = new SimpleIntegerProperty(this, "rows", 3);
  private final Map<Component, ChildContainer> panes = new WeakHashMap<>();
  private final Pane highlight = new Pane();
  private DoubleBinding placeholderWidth;
  private DoubleBinding placeholderHeight;

  @FXML
  private void initialize() {
    highlight.getStyleClass().add("grid-highlight");
    placeholderWidth = grid.widthProperty().divide(numColumns);
    placeholderHeight = grid.heightProperty().divide(numRows);
    addPlaceholders(0, numColumns.get(), 0, numRows.get());
    numColumns.addListener((__, prev, cur) -> {
      int oldNum = prev.intValue();
      int newNum = cur.intValue();
      if (newNum > oldNum) {
        addPlaceholders(oldNum, newNum, 0, numRows.get());
      } else {
        removePlaceholders(GridPane::getColumnIndex, newNum - 1);
      }
    });
    numRows.addListener((__, prev, cur) -> {
      int oldNum = prev.intValue();
      int newNum = cur.intValue();
      if (newNum > oldNum) {
        addPlaceholders(0, numColumns.get(), oldNum, newNum);
      } else {
        removePlaceholders(GridPane::getRowIndex, newNum - 1);
      }
    });
    setupDropHighlight();
  }

  /**
   * Adds placeholders for all the positions in the range [minCol, maxCol] -> [minRow, maxRow].
   *
   * @param minCol the index of the leftmost column to add placeholders to
   * @param maxCol the index of the rightmost column to add placeholders to
   * @param minRow the index of the leftmost row to add placeholders to
   * @param maxRow the index of the rightmost row to add placeholders to
   */
  private void addPlaceholders(int minCol, int maxCol, int minRow, int maxRow) {
    for (int col = minCol; col < maxCol; col++) {
      for (int row = minRow; row < maxRow; row++) {
        addPlaceholder(col, row);
      }
    }
  }

  /**
   * Adds a placeholder to the given position if one is not already present.
   *
   * @param col the column to add the placeholder to
   * @param row the row to add the placeholder to
   */
  private void addPlaceholder(int col, int row) {
    Placeholder placeholder = new Placeholder(col, row);
    boolean added = ListUtils.addIfNotPresent(grid.getChildren(), 0, placeholder);
    if (added) {
      if (nodesInCol(col).allMatch(isPlaceholder)) {
        placeholder.prefWidthProperty().bind(placeholderWidth);
      }
      if (nodesInRow(row).allMatch(isPlaceholder)) {
        placeholder.prefHeightProperty().bind(placeholderHeight);
      }
    }
  }

  /**
   * Removes all placeholders whose position (column or row index) is greater than the maximum.
   *
   * @param positionGetter the getter for the index to compare
   * @param max            the maximum allowable index
   */
  private void removePlaceholders(ToDoubleFunction<Node> positionGetter, int max) {
    grid.getChildren().stream()
        .filter(child -> child instanceof Placeholder)
        .filter(child -> positionGetter.applyAsDouble(child) > max)
        .collect(Collectors.toList())
        .forEach(grid.getChildren()::remove);
  }

  /**
   * Sets up the highlight pane used to preview the positions of dropping components.
   */
  private void setupDropHighlight() {
    grid.setOnDragEntered(e -> {
      ListUtils.addIfNotPresent(grid.getChildren(), highlight);
      pointAt(e.getX(), e.getY()).applyTo(highlight);
    });

    grid.setOnDragOver(event -> {
      GridPoint point = pointAt(event.getX(), event.getY());
      if (point == null) {
        grid.getChildren().remove(highlight);
      } else {
        ListUtils.addIfNotPresent(grid.getChildren(), highlight);
        point.applyTo(highlight);
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

    // If the component has been moved via drag-and-drop, make sure to remove it from the grid
    // before re-adding it in the new location
    grid.getChildren().remove(paneFor(component));

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
        && node != highlight; // NOPMD -- this is the correct comparison
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
        .filter(isPlaceholder.negate())
        .noneMatch(n -> GridPane.getColumnIndex(n) == col && GridPane.getRowIndex(n) == row);
  }

  private ChildContainer paneFor(Component component) {
    if (panes.containsKey(component)) {
      return panes.get(component);
    }
    ChildContainer pane = new ChildContainer(component, this);
    pane.labelPositionProperty().bindBidirectional(this.labelPositionProperty());
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
    GridPoint point = GridPoint.fromNode(container);
    grid.getChildren().remove(container);

    // Rebind placeholder sizes if this was the only component in its column or row
    if (nodesInCol(point.getCol()).allMatch(isPlaceholder)) {
      nodesInCol(point.getCol())
          .flatMap(TypeUtils.castStream(Placeholder.class))
          .forEach(placeholder -> placeholder.prefWidthProperty().bind(placeholderWidth));
    }
    if (nodesInRow(point.getRow()).allMatch(isPlaceholder)) {
      nodesInRow(point.getRow())
          .flatMap(TypeUtils.castStream(Placeholder.class))
          .forEach(placeholder -> placeholder.prefHeightProperty().bind(placeholderHeight));
    }
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
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Layout",
            Setting.of("Number of columns", numColumns, Integer.class),
            Setting.of("Number of rows", numRows, Integer.class),
            Setting.of("Label position", labelPositionProperty(), LabelPosition.class)
        )
    );
  }

  @Override
  public String getName() {
    return "Grid Layout";
  }

  /**
   * Gets a list of the component containers in this layout.
   */
  public ImmutableList<ChildContainer> getContainers() {
    return grid.getChildren().stream()
        .flatMap(TypeUtils.castStream(ChildContainer.class))
        .collect(ListUtils.toImmutableList());
  }

  public int getNumColumns() {
    return numColumns.get();
  }

  public IntegerProperty numColumnsProperty() {
    return numColumns;
  }

  public void setNumColumns(int numColumns) {
    this.numColumns.set(numColumns);
  }

  public int getNumRows() {
    return numRows.get();
  }

  public IntegerProperty numRowsProperty() {
    return numRows;
  }

  public void setNumRows(int numRows) {
    this.numRows.set(numRows);
  }

  /**
   * A placeholder for use in the grid. Placeholders are invisible and are behind all other components, and therefore
   * do not modify the UI or UX. These are used to simplify the computations for mapping drag points to grid coordinates
   * and for forcing empty columns and rows to have size (without placeholders, they would have zero width or height
   * until child components are added).
   */
  static final class Placeholder extends Pane {

    private final int col;
    private final int row;

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

}
