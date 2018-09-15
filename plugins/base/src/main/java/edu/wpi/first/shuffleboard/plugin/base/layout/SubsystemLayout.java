package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.Populatable;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.DataSourceUtils;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SubsystemType;

import com.google.common.collect.ImmutableSet;

import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

@ParametrizedController("SubsystemLayout.fxml")
public class SubsystemLayout extends ListLayout implements Populatable, Sourced {

  private final ObservableList<DataSource> sources = FXCollections.observableArrayList();

  @Override
  public String getName() {
    return "Subsystem Layout";
  }

  @Override
  public boolean supports(String sourceId) {
    DataType<?> dataType = SourceTypes.getDefault()
        .typeForUri(sourceId)
        .dataTypeForSource(DataTypes.getDefault(), sourceId);
    return getSource() != null
        && !getSource().getId().equals(sourceId)
        && sourceId.startsWith(getSource().getId())
        && dataType != DataTypes.Map
        && DataSourceUtils.isNotMetadata(sourceId);
  }

  @Override
  public boolean hasComponentFor(String sourceId) {
    return components()
        .flatMap(TypeUtils.castStream(Sourced.class))
        .map(Sourced::getSources)
        .flatMap(List::stream)
        .map(DataSource::getId)
        .anyMatch(sourceId::startsWith);
  }

  @Override
  public void addComponentFor(DataSource<?> source) {
    Components.getDefault().defaultComponentNameFor(source.getDataType())
        .flatMap(name -> Components.getDefault().createComponent(name, source))
        .ifPresent(this::addChild);
  }

  @Override
  public void addChild(Component child) {
    super.addChild(child);
    // Remove redundant source name information. If a subsystem is named "Elevator", anything
    // underneath named "Elevator/Foo" would show the "Elevator" bit again, even though it's redundant.
    if (getSource() != null) {
      String sourceName = getSource().getName();
      String prefix = sourceName.endsWith("/") ? sourceName : sourceName + "/";
      if (child.getTitle().startsWith(prefix)) {
        child.setTitle(child.getTitle().substring(prefix.length()));
      }
    }
  }

  private DataSource<?> getSource() {
    return sources.isEmpty() ? null : sources.get(0);
  }

  @Override
  public ObservableList<DataSource> getSources() {
    return sources;
  }

  @Override
  public void addSource(DataSource source) throws IncompatibleSourceException {
    if (source.getDataType() instanceof SubsystemType) {
      DataSource<?> currentSource = getSource();
      getSources().setAll(source);
      if (currentSource == null || getTitle().equals(currentSource.getName())) {
        setTitle(source.getName());
      }
    } else {
      throw new IncompatibleSourceException(ImmutableSet.of(SubsystemType.Instance), source.getDataType());
    }
  }

  @Override
  public Set<DataType> getDataTypes() {
    return ImmutableSet.of(
        SubsystemType.Instance
    );
  }

}
