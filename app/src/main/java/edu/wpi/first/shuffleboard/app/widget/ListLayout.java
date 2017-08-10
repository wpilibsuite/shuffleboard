package edu.wpi.first.shuffleboard.app.widget;

import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.Viewable;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
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

  @SuppressWarnings("FieldCanBeLocal")
  private Subscription retained;

  @FXML
  private void initialize() {
    retained = EasyBind.listBind(container.getChildren(), EasyBind.map(widgets, Viewable::getView));
  }

  @Override
  public Pane getView() {
    return root;
  }

  @Override
  public String getName() {
    return "List";
  }

  @Override
  public void addChild(Viewable widget) {
    widgets.add(widget);
  }
}
