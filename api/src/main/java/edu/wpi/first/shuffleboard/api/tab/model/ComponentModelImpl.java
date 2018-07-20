package edu.wpi.first.shuffleboard.api.tab.model;

import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.TileSize;

import java.util.HashMap;
import java.util.Map;

class ComponentModelImpl implements ComponentModel {

  private final String path;
  private final ParentModel parent;
  private String displayType;
  private final Map<String, Object> properties = new HashMap<>();
  private GridPoint preferredPosition;
  private TileSize preferredSize;

  ComponentModelImpl(String path, ParentModel parent, String displayType, Map<String, Object> properties) {
    this.path = path;
    this.parent = parent;
    this.displayType = displayType;
    setProperties(properties);
  }

  @Override
  public final String getPath() {
    return path;
  }

  @Override
  public final ParentModel getParent() {
    return parent;
  }

  @Override
  public final String getDisplayType() {
    return displayType;
  }

  @Override
  public final void setDisplayType(String displayType) {
    this.displayType = displayType;
  }

  @Override
  public final Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  public final void setProperties(Map<String, Object> properties) {
    this.properties.clear();
    this.properties.putAll(properties);
  }

  @Override
  public final GridPoint getPreferredPosition() {
    return preferredPosition;
  }

  @Override
  public final void setPreferredPosition(GridPoint preferredPosition) {
    this.preferredPosition = preferredPosition;
  }

  @Override
  public final TileSize getPreferredSize() {
    return preferredSize;
  }

  @Override
  public final void setPreferredSize(TileSize preferredSize) {
    this.preferredSize = preferredSize;
  }
}
