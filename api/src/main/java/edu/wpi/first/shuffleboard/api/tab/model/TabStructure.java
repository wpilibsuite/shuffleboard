package edu.wpi.first.shuffleboard.api.tab.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the structure of the tabs in Shuffleboard.
 */
public final class TabStructure {

  private final Map<String, TabModel> tabs = new LinkedHashMap<>();
  private final List<StructureChangeListener> structureChangeListeners = new ArrayList<>();

  /**
   * Gets the tab with the given title, creating it if it does not already exist.
   *
   * @param title the title of the tab
   *
   * @return the tab with the given title
   */
  public TabModel getTab(String title) {
    if (!tabs.containsKey(title)) {
      TabModel tab = new TabModelImpl(title);
      tabs.put(title, tab);
      dirty();
    }
    return tabs.get(title);
  }

  /**
   * Gets the tabs in the tab model. Tabs are mapped to their titles.
   */
  public Map<String, TabModel> getTabs() {
    return tabs;
  }

  public void addStructureChangeListener(StructureChangeListener listener) {
    synchronized (structureChangeListeners) {
      structureChangeListeners.add(listener);
    }
  }

  public void removeStructureChangeListener(StructureChangeListener listener) {
    synchronized (structureChangeListeners) {
      structureChangeListeners.remove(listener);
    }
  }

  public void dirty() {
    synchronized (structureChangeListeners) {
      for (StructureChangeListener listener : structureChangeListeners) {
        listener.structureChanged(this);
      }
    }
  }
}
