package edu.wpi.first.shuffleboard.api.tab.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

class ParentModelImpl implements ParentModel {

  private final Map<String, LayoutModel> layouts = new HashMap<>();
  private final Map<String, ComponentModel> children = new HashMap<>();

  @Override
  public LayoutModel getLayout(String path, String layoutType) {
    LayoutModel layout = layouts.computeIfAbsent(path, p -> new LayoutModelImpl(p, this, layoutType));
    children.put(path, layout);
    return layout;
  }

  @Override
  public ComponentModel getChild(String path) {
    if (children.containsKey(path)) {
      return children.get(path);
    }
    return layouts.values().stream()
        .map(l -> l.getChild(path))
        .filter(Objects::nonNull)
        .findFirst()
        .orElse(null);
  }

  @Override
  public void addChild(ComponentModel component) {
    children.put(component.getPath(), component);
  }

  @Override
  public Map<String, ComponentModel> getChildren() {
    return children;
  }
}
