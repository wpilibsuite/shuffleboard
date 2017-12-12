package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.Populatable;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
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
  public boolean supports(String sourceId) {
    DataType<?> dataType = SourceTypes.getDefault()
        .typeForUri(sourceId)
        .dataTypeForSource(DataTypes.getDefault(), sourceId);
    return getSource() != null
        && !getSource().getId().equals(sourceId)
        && sourceId.startsWith(getSource().getId())
        && dataType != DataTypes.Map
        && !NetworkTableUtils.isMetadata(sourceId);
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

  @Override
  public Set<DataType> getDataTypes() {
    return ImmutableSet.of(
        new SubsystemType()
    );
  }

}
