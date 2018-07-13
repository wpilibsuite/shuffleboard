package edu.wpi.first.shuffleboard.api.tab.model;

import java.util.HashMap;
import java.util.Map;

final class TabModelImpl extends ParentModelImpl implements TabModel {

  private final String title;
  private Map<String, Object> properties = new HashMap<>();

  TabModelImpl(String title) {
    this.title = title;
  }

  @Override
  public String getTitle() {
    return title;
  }

  @Override
  public Map<String, Object> getProperties() {
    return properties;
  }

  @Override
  public void setProperties(Map<String, Object> properties) {
    this.properties = properties;
  }
}
