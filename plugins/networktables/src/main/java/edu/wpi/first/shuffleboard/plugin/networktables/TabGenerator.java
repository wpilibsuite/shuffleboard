package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.tab.model.ComponentModel;
import edu.wpi.first.shuffleboard.api.tab.model.LayoutModel;
import edu.wpi.first.shuffleboard.api.tab.model.ParentModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabStructure;
import edu.wpi.first.shuffleboard.api.tab.model.WidgetModel;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSource;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Helper class for generating tabs in the UI from data in NetworkTables.
 */
final class TabGenerator {

  private final TabStructure tabs = new TabStructure();
  private final NetworkTableInstance inst;
  private int tabsListener;
  private int metadataListener;
  private int dataListener;

  TabGenerator(NetworkTableInstance inst) {
    this.inst = inst;
  }

  /**
   * Starts the generator.
   */
  public void start() {
    // Make sure all tabs exist if they're defined, even if they're empty
    NetworkTable rootMetaTable = inst.getTable("/Shuffleboard/.metadata");
    tabsListener = rootMetaTable.addEntryListener("Tabs", (table, key, entry, value, flags) -> {
      String[] tabNames = value.getStringArray();
      for (String tabName : tabNames) {
        tabs.getTab(tabName);
      }
      tabs.dirty();
    }, 0xFF);

    metadataListener = inst.addEntryListener("/Shuffleboard/.metadata/", this::metadataChanged,
        EntryListenerFlags.kImmediate | EntryListenerFlags.kNew | EntryListenerFlags.kUpdate);
    dataListener = inst.addEntryListener("/Shuffleboard", this::dataChanged,
        EntryListenerFlags.kImmediate | EntryListenerFlags.kNew);
  }

  /**
   * Stops the generator.
   */
  public void stop() {
    inst.getTable("/Shuffleboard/.metadata").removeEntryListener(tabsListener);
    inst.removeEntryListener(metadataListener);
    inst.removeEntryListener(dataListener);
  }

  /**
   * Gets the tab structure.
   */
  public TabStructure getStructure() {
    return tabs;
  }

  private void metadataChanged(EntryNotification event) {
    String name = event.name;
    List<String> metaHierarchy = NetworkTable.getHierarchy(name);
    if (metaHierarchy.size() < 5) {
      // Not metadata for a component or a tab, bail
      return;
    }
    List<String> realHierarchy = NetworkTable.getHierarchy(realPath(name));
    TabModel tab = tabs.getTab(NetworkTable.basenameKey(realHierarchy.get(2)));
    if (name.endsWith("/PreferredComponent")) {
      String real = realHierarchy.get(realHierarchy.size() - 2);
      String preferredComponentType = event.getEntry().getValue().getString();
      if (tab.getChild(real) == null) {
        return;
      }
      tab.getChild(real).setDisplayType(preferredComponentType);
    }
    if (name.endsWith("Size")) {
      String real = realHierarchy.get(realHierarchy.size() - 2);
      if (tab.getChild(real) == null) {
        // No component yet
        return;
      }
      double[] size = inst.getEntry(name).getDoubleArray(new double[0]);
      if (size.length == 2) {
        tab.getChild(real).setPreferredSize(new TileSize((int) size[0], (int) size[1]));
      }
    }
    if (name.endsWith("Position")) {
      String real = realHierarchy.get(realHierarchy.size() - 2);
      if (tab.getChild(real) == null) {
        // No component yet
        return;
      }
      double[] pos = inst.getEntry(name).getDoubleArray(new double[0]);
      if (pos.length == 2) {
        tab.getChild(real).setPreferredPosition(new GridPoint((int) pos[0], (int) pos[1]));
      }
    }
    if (name.matches("^.+/Properties/[^/]+$")) {
      String real = realHierarchy.get(realHierarchy.size() - 3);
      String propsTableName = metaHierarchy.get(metaHierarchy.size() - 2);
      NetworkTable propsTable = inst.getTable(propsTableName);
      Map<String, Object> properties = propsTable.getKeys()
          .stream()
          .collect(Collectors.toMap(t -> t, k -> propsTable.getEntry(k).getValue().getValue()));
      if (NetworkTable.basenameKey(real).equals(tab.getTitle())) {
        tab.setProperties(properties);
      } else {
        if (tab.getChild(real) == null) {
          // No component yet to set the properties for. Its properties will be set once it appears
          return;
        }
        tab.getChild(real).setProperties(properties);
      }
    }
    tabs.dirty();
  }

  private void dataChanged(EntryNotification event) {
    for (String tabName : inst.getEntry("/Shuffleboard/.metadata/Tabs").getStringArray(null)) {
      // Make sure the tabs exist, and in the order specified
      tabs.getTab(tabName);
    }
    if (event.name.startsWith("/Shuffleboard/.metadata/")) {
      return;
    }
    List<String> hierarchy = NetworkTable.getHierarchy(event.name);
    if (hierarchy.size() < 3) {
      // Not enough data
      return;
    }
    if (NetworkTable.basenameKey(event.name).startsWith(".")) {
      // Metadata changed; we don't need to worry about it
      return;
    }
    updateStructure(hierarchy);
    tabs.dirty();
  }

  private void updateStructure(List<String> hierarchy) {
    // 0='/', 1='/Shuffleboard', 2='/Shuffleboard/<Tab>'
    TabModel tab = tabs.getTab(NetworkTable.basenameKey(hierarchy.get(2)));
    ParentModel parent = tab;
    int index = 0;
    boolean end = false;
    for (String path : hierarchy) {
      if (index == 0) {
        // Skip leading "/"
        index++;
        continue;
      }
      NetworkTable table = inst.getTable(path);
      if (table.getKeys().contains(".type")) {
        String type = table.getEntry(".type").getString(null);
        switch (type) {
          case "ShuffleboardTab":
            tab.setProperties(properties(path));
            break;
          case "ShuffleboardLayout":
            String layoutType = preferredComponent(path, null);
            if (layoutType == null) {
              // No component specified for this layout - its children will be placed in its parent container
              continue;
            }
            LayoutModel layout = parent.getLayout(path, layoutType);
            setSizeAndPosition(path, layout);
            layout.setProperties(properties(path));
            parent = layout;
            break;
          default:
            end = true;
            WidgetModel widget = parent.getOrCreate(path, sourceForPath(path), preferredComponent(path, type), properties(path));
            setSizeAndPosition(path, widget);
            break;
        }
      } else if (index > 1) {
        end = true;
        WidgetModel widget = parent.getOrCreate(path, sourceForPath(path), preferredComponent(path, null), properties(path));
        setSizeAndPosition(path, widget);
      }
      index++;
      if (end) {
        break;
      }
    }
  }

  // Helper functions

  /**
   * Generates a source supplier for data at a given path.
   *
   * @param path the path to the data to get a data source for
   */
  private Supplier<? extends DataSource<?>> sourceForPath(String path) {
    return () -> NetworkTableSource.forKey(path);
  }

  /**
   * Gets the table containing the metadata for a component.
   *
   * @param realPath the full path to the value to get the data for, eg "/Shuffleboard/Tab/LayoutX/FooData"
   */
  private NetworkTable metaTable(String realPath) {
    return inst.getTable(realPath.replaceFirst("^/Shuffleboard/", "/Shuffleboard/.metadata/"));
  }

  /**
   * Maps a metadata path to a path to the real data.
   *
   * @param metaPath the path to the metadata
   */
  private String realPath(String metaPath) {
    return (metaPath.replaceFirst("^/Shuffleboard/.metadata/", "/Shuffleboard/"));
  }

  /**
   * Gets the preferred component for a component.
   *
   * @param realPath the path to the component
   * @param fallback the fallback component type if no preferred component is specified in the metadata table
   */
  private String preferredComponent(String realPath, String fallback) {
    return metaTable(realPath).getEntry("PreferredComponent").getString(fallback);
  }

  /**
   * Gets the properties of a component, or an empty map if no properties are specified in the metadata table.
   *
   * @param realPath the path to the component
   */
  private Map<String, Object> properties(String realPath) {
    NetworkTable table = metaTable(realPath);
    Map<String, Object> props = new LinkedHashMap<>();
    if (table.containsSubTable("Properties")) {
      NetworkTable propsTable = table.getSubTable("Properties");
      for (String k : propsTable.getKeys()) {
        props.put(k, propsTable.getEntry(k).getValue().getValue());
      }
    }
    return props;
  }

  private void setSizeAndPosition(String path, ComponentModel component) {
    NetworkTable metaTable = metaTable(path);
    if (metaTable.containsKey("Size")) {
      double[] size = metaTable.getEntry("Size").getDoubleArray(new double[0]);
      if (size.length == 2) {
        component.setPreferredSize(new TileSize((int) size[0], (int) size[1]));
      }
    }
    if (metaTable.containsKey("Position")) {
      double[] pos = metaTable.getEntry("Position").getDoubleArray(new double[0]);
      if (pos.length == 2) {
        component.setPreferredPosition(new GridPoint((int) pos[0], (int) pos[1]));
      }
    }
  }
}
