package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.util.ListUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.LayoutBase;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

@ParametrizedController("GridLayout.fxml")
public class GridLayout extends LayoutBase implements Layout {

  @FXML
  private Pane root;
  @FXML
  private TableView<Component> table;
  @FXML
  private TableColumn<Component, String> titleColumn;
  @FXML
  private TableColumn<Component, Node> viewColumn;

  @FXML
  private void initialize() {
    titleColumn.setCellValueFactory(f -> f.getValue().titleProperty());
    titleColumn.setCellFactory(c -> new TableCell<Component, String>() {

      private final EditableLabel label = new EditableLabel();

      {
        label.textProperty().addListener((__, old, text) -> {
          Component component = getComponent();
          if (component != null) {
            component.setTitle(text);
          }
        });
      }

      @Override
      protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
        } else {
          label.setText(item);
          setGraphic(label);
        }
      }

      Component getComponent() {
        if (getTableView() == null) {
          return null;
        }
        if (getTableRow() == null) {
          return null;
        }
        if (getTableRow().getIndex() < 0) {
          return null;
        }
        return getTableView().getItems().get(getTableRow().getIndex());
      }
    });

    viewColumn.setCellValueFactory(f -> new ReadOnlyObjectWrapper<>(f.getValue().getView()));
    viewColumn.setCellFactory(c -> new TableCell<Component, Node>() {
      @Override
      protected void updateItem(Node item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
          setGraphic(null);
        } else {
          setGraphic(item);
        }
      }
    });
    viewColumn.setSortable(false); // Views don't have any sensible sort order
  }

  @Override
  protected void addComponentToView(Component component) {
    table.getItems().add(component);
    ActionList.registerSupplier(component.getView(), () -> actionsForComponent(component));
  }

  @Override
  protected void removeComponentFromView(Component component) {
    table.getItems().remove(component);
  }

  @Override
  protected void replaceInPlace(Component existing, Component replacement) {
    ListUtils.replaceIn(table.getItems())
        .replace(existing)
        .with(replacement);
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public String getName() {
    return "Grid Layout";
  }
}
