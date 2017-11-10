package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.Populatable;
import edu.wpi.first.shuffleboard.api.components.ActionList;
import edu.wpi.first.shuffleboard.api.components.EditableLabel;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SubsystemType;

import com.google.common.collect.ImmutableSet;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Stream;

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
public class SubsystemLayout implements Layout, Populatable, Sourced {

  @FXML
  private StackPane root;
  @FXML
  private VBox container;

  private final ObservableList<Component> children = FXCollections.observableArrayList();
  private List<Component> hidden = new ArrayList<>();
  private final StringProperty title = new SimpleStringProperty(this, "title", "Subsystem");

  private Subscription retained; //NOPMD field due to GC
  private final ObservableList<DataSource> sources = FXCollections.observableArrayList();

  private WeakHashMap<Component, Pane> panes = new WeakHashMap<>();

  @FXML
  private void initialize() {
    retained = EasyBind.listBind(container.getChildren(), EasyBind.map(children, this::paneFor));

    ActionList.registerSupplier(container, () -> {
      ActionList al = ActionList.withName(getTitle());
      if (!hidden.isEmpty()) {
        al.addAction("Unhide all", () -> {
          children.addAll(hidden);
          hidden.clear();
        });
      }
      return al;
    });
  }

  private Pane paneFor(Component component) {
    if (panes.containsKey(component)) {
      return panes.get(component);
    }

    BorderPane pane = new BorderPane(component.getView());
    ActionList.registerSupplier(pane, () -> this.actionsForComponent(component));
    pane.getStyleClass().add("layout-stack");
    EditableLabel label = new EditableLabel(component.titleProperty());
    label.getStyleClass().add("layout-label");
    BorderPane.setAlignment(label, Pos.TOP_LEFT);
    pane.setBottom(label);
    panes.put(component, pane);
    return pane;
  }

  private ActionList actionsForComponent(Component component) {
    return ActionList.withName(component.getTitle())
        .addAction("Hide in layout", () -> {
          children.remove(component);
          hidden.add(component);
        });
  }

  @Override
  public Collection<Component> getChildren() {
    return children;
  }

  @Override
  public void addChild(Component child) {
    if (getSource() != null) {
      final String sourceName = getSource().getName();
      if (child.getTitle().startsWith(sourceName)) {
        // Remove leading redundant information
        child.setTitle(NetworkTableUtils.normalizeKey(child.getTitle().substring(sourceName.length()), false));
      }
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

  @Override
  public boolean supports(DataSource<?> source) {
    return getSource() != null
        && getSource() != source
        && source.getName().startsWith(getSource().getName())
        && !NetworkTableUtils.isMetadata(source.getId());
  }

  @Override
  public boolean hasComponentFor(DataSource<?> source) {
    return Stream.concat(components(), hidden.stream())
        .flatMap(TypeUtils.castStream(Sourced.class))
        .map(Sourced::getSources)
        .flatMap(List::stream)
        .map(DataSource::getId)
        .anyMatch(source.getId()::startsWith);
  }

  @Override
  public void addComponentFor(DataSource<?> source) {
    Components.getDefault().defaultComponentNameFor(source.getDataType())
        .flatMap(name -> Components.getDefault().createComponent(name, source))
        .ifPresent(this::addChild);
  }

  DataSource<?> getSource() {
    return sources.isEmpty() ? null : sources.get(0);
  }

  @Override
  public ObservableList<DataSource> getSources() {
    return sources;
  }

  @Override
  public void addSource(DataSource source) throws IncompatibleSourceException {
    if (source.getDataType() instanceof SubsystemType) {
      getSources().setAll(source);
      setTitle(source.getName());
    } else {
      throw new IncompatibleSourceException(ImmutableSet.of(new SubsystemType()), source.getDataType());
    }
  }

}
