package edu.wpi.first.shuffleboard.app.widget;

import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.Viewable;
import edu.wpi.first.shuffleboard.app.components.EditableLabel;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import java.util.Collection;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
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

  private ObservableList<Viewable> widgets = FXCollections.observableArrayList();

  private StringProperty title = new SimpleStringProperty(this, "title", "List");

  @SuppressWarnings("FieldCanBeLocal")
  private Subscription retained;

  @FXML
  private void initialize() {
    retained = EasyBind.listBind(container.getChildren(), EasyBind.map(widgets, this::paneFor));
  }

  private Pane paneFor(Viewable widget) {
    BorderPane pane = new BorderPane(widget.getView());
    pane.getStyleClass().add("layout--stack");
    EditableLabel label = new EditableLabel(widget.nameProperty());
    label.getStyleClass().add("layout--label");
    BorderPane.setAlignment(label, Pos.TOP_LEFT);
    pane.setBottom(label);
    return pane;
  }

  @Override
  public Collection<Viewable> getChildren() {
    return widgets;
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public Property<String> nameProperty() {
    return title;
  }

  @Override
  public void addChild(Viewable widget) {
    widgets.add(widget);
  }
}
