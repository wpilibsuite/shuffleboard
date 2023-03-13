package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.appPlatter;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.tab.model.ComponentModel;
import edu.wpi.first.shuffleboard.api.tab.model.LayoutModel;
import edu.wpi.first.shuffleboard.api.tab.model.ParentModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabStructure;
import edu.wpi.first.shuffleboard.api.tab.model.WidgetModel;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.util.function.MappableSupplier;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSource;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTableType;
import edu.wpi.first.networktables.NetworkTableValue;
import edu.wpi.first.networktables.StringArraySubscriber;
import edu.wpi.first.networktables.Topic;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class for generating tabs in the UI from data in NetworkTables.
 */
@SuppressWarnings("PMD.GodClass")
final class TabGenerator {

  public static final String ROOT_TABLE_NAME = "/Shuffleboard";
  public static final String METADATA_TABLE_NAME = ROOT_TABLE_NAME + "/.metadata";
  public static final String PREF_COMPONENT_ENTRY_NAME = "PreferredComponent";
  public static final String PROPERTIES_TABLE_NAME = "Properties";
  public static final String TABS_ENTRY_KEY = "Tabs";
  public static final String TABS_ENTRY_PATH = METADATA_TABLE_NAME + "/" + TABS_ENTRY_KEY;
  public static final String POSITION_ENTRY_NAME = "Position";
  public static final String SIZE_ENTRY_NAME = "Size";
  public static final String SELECTED_ENTRY_NAME = "Selected";
  public static final String VISIBILE_ENTRY_NAME = "ContentVisible";
  public static final String OPACITY_ENTRY_NAME = "Opacity";

  public static final String TAB_TYPE = "ShuffleboardTab";
  public static final String LAYOUT_TYPE = "ShuffleboardLayout";

  private final TabStructure tabs = new TabStructure();
  private final NetworkTableInstance inst;
  private StringArraySubscriber tabsSubscriber;
  private int tabsListener;
  private int metadataListener;
  private int dataListener;
  private final Components componentRegistry;
  private final BooleanProperty isDynamic;

  TabGenerator(NetworkTableInstance inst, Components componentRegistry, BooleanProperty isDynamic) {
    this.inst = inst;
    this.componentRegistry = componentRegistry;
    this.isDynamic = isDynamic;
  }

  TabGenerator(NetworkTableInstance inst, Components componentRegistry) {
    this.inst = inst;
    this.componentRegistry = componentRegistry;
    this.isDynamic = new SimpleBooleanProperty(false);
  }

  /**
   * Starts the generator.
   */
  public void start() {
    // Make sure all tabs exist if they're defined, even if they're empty
    NetworkTable rootMetaTable = inst.getTable(METADATA_TABLE_NAME);
    tabsSubscriber = rootMetaTable.getStringArrayTopic(TABS_ENTRY_KEY).subscribe(new String[] {});
    tabsListener = inst.addListener(tabsSubscriber,
        EnumSet.of(NetworkTableEvent.Kind.kValueAll, NetworkTableEvent.Kind.kImmediate), event -> {
          for (String tabName : event.valueData.value.getStringArray()) {
            tabs.getTab(tabName);
          }
          tabs.dirty();
        });

    metadataListener = inst.addListener(
        new String[] { METADATA_TABLE_NAME + "/" },
        EnumSet.of(NetworkTableEvent.Kind.kValueAll, NetworkTableEvent.Kind.kImmediate),
        this::metadataChanged);
    dataListener = inst.addListener(
        new String[] { ROOT_TABLE_NAME + "/" },
        EnumSet.of(NetworkTableEvent.Kind.kValueAll, NetworkTableEvent.Kind.kImmediate),
        this::dataChanged);
  }

  /**
   * Stops the generator.
   */
  public void stop() {
    tabsSubscriber.close();
    inst.removeListener(tabsListener);
    inst.removeListener(metadataListener);
    inst.removeListener(dataListener);
  }

  /**
   * Gets the tab structure.
   */
  public TabStructure getStructure() {
    return tabs;
  }

  private void metadataChanged(NetworkTableEvent event) {
    String name = event.valueData.getTopic().getName();

    // Special case for global metadata, not tab or widget data
    if (name.equals("/Shuffleboard/.metadata/Selected")) {
      // If the value can be parsed as an int, assume it's the tab index, otherwise
      // assume tab title.
      String str = event.valueData.value.getString();
      try {
        tabs.setSelectedTab(Integer.parseInt(str));
      } catch (NumberFormatException e) {
        tabs.setSelectedTab(str);
      }
      return;
    }

    List<String> metaHierarchy = NetworkTable.getHierarchy(name);
    if (metaHierarchy.size() < 5) {
      // Data not nested deep enough, not metadata for a component or a tab, bail
      return;
    }
    List<String> realHierarchy = NetworkTable.getHierarchy(realPath(name));
    TabModel tab = tabs.getTab(NetworkTable.basenameKey(realHierarchy.get(2)));

    // Component type
    if (name.endsWith("/" + PREF_COMPONENT_ENTRY_NAME)) {
      String real = realHierarchy.get(realHierarchy.size() - 2);
      if (tab.getChild(real) == null) {
        return;
      }
      ComponentModel model = tab.getChild(real);
      model.setDisplayType(event.valueData.value.getString());
      if (isDynamic.get()) {componentRegistry.getWidgets(model)
          .forEach(widget -> {
            var tile = widget.getView().getParent().getParent();
            appPlatter.getInstance().notifyComponentListeners(tile, event.valueData.value.getString());
          });
        }
    }

    // Component size
    if (name.endsWith("/" + SIZE_ENTRY_NAME)) {
      String real = realHierarchy.get(realHierarchy.size() - 2);
      if (tab.getChild(real) == null) {
        // No component yet
        return;
      }
      double[] size = event.valueData.value.getDoubleArray();
      if (size.length == 2) {
        ComponentModel model = tab.getChild(real);
        model.setPreferredSize(new TileSize((int) size[0], (int) size[1]));
        if (isDynamic.get()) {componentRegistry.getWidgets(model)
            .forEach(widget -> {
              var tile = widget.getView().getParent().getParent();
              appPlatter.getInstance().notifySizeListeners(tile, new TileSize((int) size[0], (int) size[1]));
            });
          }
      }
    }

    // Component position
    if (name.endsWith("/" + POSITION_ENTRY_NAME)) {
      String real = realHierarchy.get(realHierarchy.size() - 2);
      if (tab.getChild(real) == null) {
        // No component yet
        return;
      }
      double[] pos = event.valueData.value.getDoubleArray();
      if (pos.length == 2) {
        ComponentModel model = tab.getChild(real);
        model.setPreferredPosition(new GridPoint((int) pos[0], (int) pos[1]));
        if (isDynamic.get()) {componentRegistry.getWidgets(model)
            .forEach(widget -> {
              var tile = widget.getView().getParent().getParent();
              appPlatter.getInstance().notifyPositionListeners(tile, new GridPoint((int) pos[0], (int) pos[1]));
            });
          }
      }
    }

    // Component opacity
    if (name.endsWith("/" + OPACITY_ENTRY_NAME)) {
      String real = realHierarchy.get(realHierarchy.size() - 2);
      if (tab.getChild(real) == null) {
        // No component yet
        return;
      }
      ComponentModel model = tab.getChild(real);
      double opacity = event.valueData.value.getDouble();
      model.setOpacity(opacity);
      if (isDynamic.get()) {componentRegistry.getWidgets(model)
          .forEach(widget -> {
            var tile = widget.getView().getParent().getParent();
            appPlatter.getInstance().notifyOpacityListeners(tile, opacity);
          });
        }
    }

    // Component visibility
    if (name.endsWith("/" + VISIBILE_ENTRY_NAME)) {
      String real = realHierarchy.get(realHierarchy.size() - 2);
      if (tab.getChild(real) == null) {
        // No component yet
        return;
      }
      ComponentModel model = tab.getChild(real);
      boolean visible = event.valueData.value.getBoolean();
      model.setContentVisibility(visible);
      if (isDynamic.get())  {componentRegistry.getWidgets(model)
          .forEach(widget -> {
            var tile = widget.getView().getParent().getParent();
            appPlatter.getInstance().notifyVisibilityListeners(tile, visible);
          });
        }
    }

    // Component (or tab) properties
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
          // No component yet to set the properties for. Its properties will be set once
          // it appears
          return;
        }
        tab.getChild(real).setProperties(properties);
      }
    }

    tabs.dirty();
  }

  private void dataChanged(NetworkTableEvent event) {
    for (String tabName : inst.getEntry(TABS_ENTRY_PATH).getStringArray(new String[0])) {
      // Make sure the tabs exist, and in the order specified
      tabs.getTab(tabName);
    }
    String name = event.valueData.getTopic().getName();
    if (name.startsWith(METADATA_TABLE_NAME)) {
      return;
    }
    List<String> hierarchy = NetworkTable.getHierarchy(name);
    if (hierarchy.size() < 3) {
      // Not enough data
      return;
    }
    if (name.contains("/.")) {
      // The entry is metadata, but may be the only entry in a table, so make sure
      // it's updated correctly
      var tables = hierarchy.stream()
          .takeWhile(s -> !s.contains("/."))
          .collect(Collectors.toList());
      if (tables.size() >= 3) {
        updateFrom(tables);
      }
      return;
    }
    updateFrom(hierarchy);
  }

  private void updateFrom(List<String> tables) {
    updateStructure(tables);
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
          case TAB_TYPE:
            tab.setProperties(properties(path));
            break;
          case LAYOUT_TYPE:
            String layoutType = preferredComponent(path, () -> null);
            if (layoutType == null) {
              // No component specified for this layout - its children will be placed in its
              // parent container
              continue;
            }
            LayoutModel layout = parent.getLayout(path, layoutType);
            setSizeAndPosition(path, layout);
            layout.setProperties(properties(path));
            parent = layout;
            break;
          default:
            end = true;
            updateWidget(parent, path);
            break;
        }
      } else if (index > 1) {
        end = true;
        updateWidget(parent, path);
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
  private MappableSupplier<? extends DataSource<?>> sourceForPath(String path) {
    Topic[] topics = inst.getTopics("/", NetworkTableType.kString.getValue());
    Optional<String> customUri = Stream.of(topics)
        .filter(t -> t.getName().equals(path + "/.ShuffleboardURI"))
        .map(t -> inst.getEntry(t.getName()).getString(null))
        .filter(Objects::nonNull)
        .findFirst();
    if (customUri.isPresent()) {
      return () -> SourceTypes.getDefault().forUri(customUri.get());
    } else {
      return () -> NetworkTableSource.forKey(path);
    }
  }

  /**
   * Gets the table containing the metadata for a component.
   *
   * @param realPath the full path to the value to get the data for, eg
   *                 "/Shuffleboard/Tab/LayoutX/FooData"
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
    return metaPath.replaceFirst("^/Shuffleboard/.metadata/", "/Shuffleboard/");
  }

  /**
   * Gets the preferred component for a component.
   *
   * @param realPath the path to the component
   * @param fallback the fallback component type if no preferred component is
   *                 specified in the metadata table
   */
  private String preferredComponent(String realPath, Supplier<String> fallback) {
    NetworkTableValue value = metaTable(realPath).getEntry(PREF_COMPONENT_ENTRY_NAME).getValue();
    if (value.isString()) {
      return value.getString();
    } else {
      return fallback.get();
    }
  }

  /**
   * Gets the properties of a component, or an empty map if no properties are
   * specified in the metadata table.
   *
   * @param realPath the path to the component
   */
  private Map<String, Object> properties(String realPath) {
    NetworkTable table = metaTable(realPath);
    Map<String, Object> props = new LinkedHashMap<>();
    if (table.containsSubTable(PROPERTIES_TABLE_NAME)) {
      NetworkTable propsTable = table.getSubTable(PROPERTIES_TABLE_NAME);
      for (String k : propsTable.getKeys()) {
        props.put(k, propsTable.getEntry(k).getValue().getValue());
      }
    }
    return props;
  }

  /**
   * Updates the widget for the given path. If no such widget exists, one is
   * created and added to the given parent.
   *
   * @param parent the parent to add a newly created widget to
   * @param path   the path to the widget
   */
  private void updateWidget(ParentModel parent, String path) {
    var sourceSupplier = sourceForPath(path);
    WidgetModel widget = parent.getOrCreate(
        path,
        sourceSupplier,
        preferredComponent(
            path,
            sourceSupplier
                .map(DataSource::getDataType)
                .map(componentRegistry::defaultComponentNameFor)
                .map(Optional::orElseThrow)),
        properties(path));
    setSizeAndPosition(path, widget);
  }

  private void setSizeAndPosition(String path, ComponentModel component) {
    NetworkTable metaTable = metaTable(path);
    if (metaTable.containsKey(SIZE_ENTRY_NAME)) {
      double[] size = metaTable.getEntry(SIZE_ENTRY_NAME).getDoubleArray(new double[0]);
      if (size.length == 2) {
        component.setPreferredSize(new TileSize((int) size[0], (int) size[1]));
      }
    }
    if (metaTable.containsKey(POSITION_ENTRY_NAME)) {
      double[] pos = metaTable.getEntry(POSITION_ENTRY_NAME).getDoubleArray(new double[0]);
      if (pos.length == 2) {
        component.setPreferredPosition(new GridPoint((int) pos[0], (int) pos[1]));
      }
    }
  }
}
