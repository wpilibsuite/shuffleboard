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

  private int selectedTabIndex = -1;
  private String selectedTabTitle = null;

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
    if (selectedTabIndex != tabIndex) {
      selectedTabIndex = tabIndex;
      selectedTabTitle = null;
      dirty();
    }
  }

  /**
   * Sets the currently selected tab.
   *
   * @param title the title of the tab to select
   */
  public void setSelectedTab(String title) {
    selectedTabTitle = title;
    selectedTabIndex = -1;
    dirty();
  }

  /**
   * Gets the currently selected tab, or -1 if no tab is currently selected by index.
   *
   * @return the index of the currently selected tab
   *
   * @see #getSelectedTabTitle()
   */
  public int getSelectedTabIndex() {
    return selectedTabIndex;
  }

  /**
   * Gets the currently selected tab title, or {@code null} if no tab is currently selected by title.
   *
   * @return the title of the currently selected tab
   *
   * @see #getSelectedTabIndex()
   */
  public String getSelectedTabTitle() {
    return selectedTabTitle;
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
