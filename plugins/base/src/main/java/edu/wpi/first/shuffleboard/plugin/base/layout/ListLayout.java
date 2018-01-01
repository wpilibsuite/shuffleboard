package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.components.ExtendedPropertySheet;
import edu.wpi.first.shuffleboard.api.util.ListUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import java.util.Collection;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

@ParametrizedController("ListLayout.fxml")
public class ListLayout implements Layout {

  @FXML
  private StackPane root;

  @FXML
  private VBox container;

  private final ObservableList<Component> widgets = FXCollections.observableArrayList();

  private final StringProperty title = new SimpleStringProperty(this, "title", "List");
  private Subscription retained; //NOPMD field due to GC

  private final WeakHashMap<Component, Pane> panes = new WeakHashMap<>();

  @FXML
  protected void initialize() {
    retained = EasyBind.listBind(container.getChildren(), EasyBind.map(widgets, this::paneFor));
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
    pane.setBottom(label);

    panes.put(component, pane);
    return pane;
  }

  private ActionList actionsForComponent(Component component) {
    ActionList actions = ActionList.withName(component.getTitle());
    int index = widgets.indexOf(component);

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
        dialog.getDialogPane().getStylesheets().setAll(root.getScene().getRoot().getStylesheets());
        dialog.getDialogPane().setContent(new BorderPane(propertySheet));
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.setResultConverter(button -> button);
        dialog.showAndWait();
      });
      actions.addNested(createChangeMenusForWidget(widget));
    }

    actions.addAction("Remove from list", () -> {
      widgets.remove(index);
    });

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

  /**
   * Creates all the menus needed for changing a widget to a different type.
   */
  private ActionList createChangeMenusForWidget(Widget widget) {
    ActionList list = ActionList.withName("Show as...");

    widget.getSources().stream()
        .map(s -> Components.getDefault().componentNamesForSource(s))
        .flatMap(List::stream)
        .sorted()
        .distinct()
        .forEach(name -> list.addAction(
            name,
            name.equals(widget.getName()) ? new Label("✓") : null,
            () -> {
              // no need to change it if it's already the same type
              if (!name.equals(widget.getName())) {
                Components.getDefault()
                    .createWidget(name, widget.getSources())
                    .ifPresent(w -> {
                      w.setTitle(widget.getTitle());
                      ListUtils.replaceIn(widgets)
                          .replace(widget)
                          .with(w);
                    });
              }
            }));
    return list;
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
  public Property<String> titleProperty() {
    return title;
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
