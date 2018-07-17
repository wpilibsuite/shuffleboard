package edu.wpi.first.shuffleboard.api.tab.model;

import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.TileSize;

import java.util.Map;

class ComponentModelImpl implements ComponentModel {

  private final String path;
  private final ParentModel parent;
  private final String type;
  private String displayType;
  private Map<String, Object> properties;
  private GridPoint preferredPosition;
  private TileSize preferredSize;

  ComponentModelImpl(String path, ParentModel parent, String type, String displayType, Map<String, Object> properties) {
    this.path = path;
    this.parent = parent;
    this.type = type;
    this.displayType = displayType;
    this.properties = properties;
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public ParentModel getParent() {
    return parent;
  }

  @Override
  public String getType() {
    return type;
  }

  @Override
  public String getDisplayType() {
    return displayType;
  }

  @Override
  public void setDisplayType(String displayType) {
    this.displayType = displayType;
  }

  @Override
  public Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }

  @Override
  public GridPoint getPreferredPosition() {
    return preferredPosition;
  }

  @Override
  public void setPreferredPosition(GridPoint preferredPosition) {
    this.preferredPosition = preferredPosition;
  }

  @Override
  public TileSize getPreferredSize() {
    return preferredSize;
  }

  @Override
  public void setPreferredSize(TileSize preferredSize) {
    this.preferredSize = preferredSize;
  }
}
