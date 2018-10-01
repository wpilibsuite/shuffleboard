package edu.wpi.first.shuffleboard.api.tab.model;

import com.google.common.collect.Iterables;

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

  private int selectedIndex = -1;

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

  /**
   * Sets the currently selected tab.
   *
   * @param tabIndex the index of the tab to select
   */
  public void setSelectedTab(int tabIndex) {
    if (selectedIndex != tabIndex) {
      selectedIndex = tabIndex;
      dirty();
    }
  }

  /**
   * Sets the currently selected tab.
   *
   * @param title the title of the tab to select
   */
  public void setSelectedTab(String title) {
    if (tabs.containsKey(title)) {
      setSelectedTab(Iterables.indexOf(tabs.keySet(), title::equals));
    }
  }

  /**
   * Gets the currently selected tab.
   *
   * @return the index of the currently selected tab
   */
  public int getSelectedTab() {
    return selectedIndex;
  }

  /**
   * Adds a listener to listen for changes to the tab structure.
   *
   * @param listener the listener to add
   */
  public void addStructureChangeListener(StructureChangeListener listener) {
    synchronized (structureChangeListeners) {
      structureChangeListeners.add(listener);
    }
  }

  /**
   * Removes a listener from structure changes.
   *
   * @param listener the listener to remove
   */
  public void removeStructureChangeListener(StructureChangeListener listener) {
    synchronized (structureChangeListeners) {
      structureChangeListeners.remove(listener);
    }
  }

  /**
   * Marks the structure as having changed. This will fire all registered structure change listeners.
   */
  public void dirty() {
    synchronized (structureChangeListeners) {
      for (StructureChangeListener listener : structureChangeListeners) {
        listener.structureChanged(this);
      }
    }
  }
}
