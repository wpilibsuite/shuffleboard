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

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

@ParametrizedController("GridLayout.fxml")
public class GridLayout extends LayoutBase {

  private final ObservableList<Component> components = FXCollections.observableArrayList();

  @FXML
  private Pane root;
  @FXML
  private GridPane grid;

  private final IntegerProperty numColumns = new SimpleIntegerProperty(this, "numColumns", 1);
  private final IntegerProperty numRows = new SimpleIntegerProperty(this, "numRows", 1);
  private final Map<Component, Pane> panes = new WeakHashMap<>();

  @FXML
  private void initialize() {
    components.addListener((ListChangeListener<Component>) c -> {
      while (c.next()) {
        if (c.wasReplaced()) {
          // ignore
          for (int i = 0; i < c.getAddedSize(); i++) {
            Component removed = c.getRemoved().get(i);
            Component added = c.getAddedSubList().get(i);
            Pane oldPane = panes.remove(removed);
            Pane newPane = paneFor(added);
            GridPane.setRowIndex(newPane, GridPane.getRowIndex(oldPane));
            GridPane.setColumnIndex(newPane, GridPane.getColumnIndex(oldPane));
            ListUtils.replaceIn(grid.getChildren())
                .replace(oldPane)
                .with(newPane);
          }
        } else if (c.wasAdded()) {
          for (Component added : c.getAddedSubList()) {
            Pane pane = paneFor(added);
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
            if (!anyOpen) {
              // Add another row
              numRows.set(numRows.get() + 1);
              GridPane.setRowIndex(pane, numRows.get() - 1);
              GridPane.setColumnIndex(pane, 0);
            }
            grid.getChildren().add(pane);
          }
        } else if (c.wasRemoved()) {
          c.getRemoved().stream()
              .map(panes::remove)
              .forEach(grid.getChildren()::remove);
        }
      }
    });
  }

  private boolean isOpen(int col, int row) {
    return grid.getChildren().stream()
        .filter(n -> GridPane.getColumnIndex(n) != null)
        .noneMatch(n -> GridPane.getColumnIndex(n) == col && GridPane.getRowIndex(n) == row);
  }

  private Pane paneFor(Component component) {
    if (panes.containsKey(component)) {
      return panes.get(component);
    }
    BorderPane pane = new BorderPane();
    pane.getStyleClass().add("layout-stack");
    pane.setCenter(component.getView());
    EditableLabel label = new EditableLabel(component.titleProperty());
    label.getStyleClass().add("layout-label");
    pane.setLeft(label);
    GridPane.setRowIndex(pane, numRows.get() - 1);
    ActionList.registerSupplier(pane, () -> actionsForComponent(component));
    panes.put(component, pane);
    return pane;
  }

  @Override
  protected void addComponentToView(Component component) {
    components.add(component);
  }

  @Override
  protected void removeComponentFromView(Component component) {
    components.remove(component);
  }

  @Override
  protected void replaceInPlace(Component existing, Component replacement) {
    ListUtils.replaceIn(components)
        .replace(existing)
        .with(replacement);
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public List<Property<?>> getProperties() {
    return ImmutableList.of(
        numColumns,
        numRows
    );
  }

  @Override
  public String getName() {
    return "Grid Layout";
  }
}
