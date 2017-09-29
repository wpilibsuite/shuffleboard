package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

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

@ParametrizedController("SubsystemLayout.fxml")
public class SubsystemLayout implements Layout {

  @FXML
  private StackPane root;
  @FXML
  private VBox container;

  private final ObservableList<Component> children = FXCollections.observableArrayList();

  private final StringProperty title = new SimpleStringProperty(this, "title", "Subsystem");

  private Subscription retained; //NOPMD field due to GC

  @FXML
  private void initialize() {
    retained = EasyBind.listBind(container.getChildren(), EasyBind.map(children, this::paneFor));
  }

  private Pane paneFor(Component component) {
    BorderPane pane = new BorderPane(component.getView());
    pane.getStyleClass().add("layout-stack");
    EditableLabel label = new EditableLabel(component.titleProperty());
    label.getStyleClass().add("layout-label");
    BorderPane.setAlignment(label, Pos.TOP_LEFT);
    pane.setBottom(label);
    return pane;
  }

  @Override
  public Collection<Component> getChildren() {
    return children;
  }

  @Override
  public void addChild(Component child) {
    if (child.getTitle().startsWith(getTitle())) {
      child.setTitle(NetworkTableUtils.normalizeKey(child.getTitle().substring(getTitle().length()), false));
    }
    if (children.isEmpty()) {
      children.add(child);
    } else {
      List<Component> sorted = new ArrayList<>(children);
      sorted.add(child);
      sorted.sort(Comparator.comparing(Component::getTitle, AlphanumComparator.INSTANCE));
      children.add(sorted.indexOf(child), child);
    }
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
    return "Subsystem Layout";
  }

}
