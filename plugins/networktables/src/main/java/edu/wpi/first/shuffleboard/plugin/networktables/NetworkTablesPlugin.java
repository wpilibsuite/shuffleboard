package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.prefs.FlushableProperty;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.tab.model.LayoutModel;
import edu.wpi.first.shuffleboard.api.tab.model.ParentModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabModel;
import edu.wpi.first.shuffleboard.api.tab.model.TabStructure;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.util.PreferencesUtils;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

@Description(
    group = "edu.wpi.first.shuffleboard",
    name = "NetworkTables",
    version = "1.0.1",
    summary = "Provides sources and widgets for NetworkTables"
)
public class NetworkTablesPlugin extends Plugin {

  private int recorderUid = -1;
  private final NetworkTableInstance inst = NetworkTableInstance.getDefault();
  private final Preferences preferences = Preferences.userNodeForPackage(getClass());

  private final StringProperty serverId = new SimpleStringProperty(this, "server", "localhost");
  private final InvalidationListener serverSaver = __ -> PreferencesUtils.save(serverId, preferences);

  private TabStructure tabs = new TabStructure();

  private final ChangeListener<String> serverChangeListener = (observable, oldValue, newValue) -> {
    String[] value = newValue.split(":");

    /*
     * You MUST set the port number before setting the team number.
     */
    int port;
    if (value.length > 1) {
      port = Integer.parseInt(value[1]);
    } else {
      port = NetworkTableInstance.kDefaultPort;
    }

    NetworkTableUtils.shutdown(inst);
    if (value[0].matches("\\d{1,4}")) {
      inst.setServerTeam(Integer.parseInt(value[0]), port);
    } else if (value[0].isEmpty()) {
      inst.setServer("localhost", port);
    } else {
      inst.setServer(value[0], port);
    }
    inst.startClient();
    inst.startDSClient();
  };

  public NetworkTablesPlugin() {
    NetworkTableSourceType.setInstance(new NetworkTableSourceType(this));
  }

  @Override
  public void onLoad() {
    PreferencesUtils.read(serverId, preferences);
    serverId.addListener(serverSaver);

    // Automatically capture and record changes in network tables
    // This is done here because each key under N subtables would have N+1 copies
    // in the recording (eg "/a/b/c" has 2 tables and 3 copies: "/a", "/a/b", and "/a/b/c")
    // This significantly reduces the size of recording files.
    recorderUid = inst.addEntryListener("", (event) -> {
      Object value = event.value.getValue();
      DataTypes.getDefault().forJavaType(value.getClass())
          .ifPresent(type -> {
            Recorder.getInstance().record(
                NetworkTableSourceType.getInstance().toUri(event.name),
                type,
                value
            );
          });
    }, 0xFF);

    // Make sure all tabs exist if they're defined, even if they're empty
    NetworkTable rootMetaTable = inst.getTable("/Shuffleboard/.metadata");
    rootMetaTable.addEntryListener("Tabs", (table, key, entry, value, flags) -> {
      String[] tabNames = value.getStringArray();
      for (String tabName : tabNames) {
        tabs.getTab(tabName);
      }
      System.out.println("Tabs changed: " + Arrays.toString(tabNames));
      tabs.dirty();
    }, 0xFF);

    inst.addEntryListener("/Shuffleboard/.metadata/", event -> {
      String name = event.name;
      if (name.endsWith("/PreferredComponent")) {
        List<String> hierarchy = NetworkTable.getHierarchy(realPath(name));
        String real = hierarchy.get(hierarchy.size() - 2);
        System.out.println(real);
        TabModel tab = tabs.getTab(NetworkTable.basenameKey(hierarchy.get(2)));
        String preferredComponentType = event.getEntry().getValue().getString();
        System.out.println("Updating " + real + " to use " + preferredComponentType);
        if (tab.getChild(real) == null) {
          System.out.println("  No component yet");
          return;
        }
        tab.getChild(real).setDisplayType(preferredComponentType);
      }
      tabs.dirty();
    }, 0xFF);

    inst.addEntryListener("/Shuffleboard", event -> {
      // Make sure the tabs exist, and in the order specified
      for (String tabName : inst.getEntry("/Shuffleboard/.metadata/Tabs").getStringArray(null)) {
        tabs.getTab(tabName);
      }
      if (event.name.startsWith("/Shuffleboard/.metadata/")) {
        return;
      }
      List<String> hierarchy = NetworkTable.getHierarchy(event.name);
      if (hierarchy.size() < 3) {
        System.out.println("Not enough data: " + event.name);
        return;
      }
      if (NetworkTable.basenameKey(event.name).startsWith(".")) {
        System.out.println("Metadata changed, don't care: " + event.name);
        return;
      }
      TabModel tab = tabs.getTab(NetworkTable.basenameKey(hierarchy.get(2))); // 0='/', 1='/Shuffleboard', 2='/Shuffleboard/<Tab>'
      ParentModel parent = tab;
      System.out.println("----------------");
      System.out.println(hierarchy);
      int index = 0;
      boolean end = false;
      for (String path : hierarchy) {
        if (index == 0) {
          // Skip leading "/"
          index++;
          continue;
        }
        System.out.println(path);
        NetworkTable table = inst.getTable(path);
        if (table.getKeys().contains(".type")) {
          String type = table.getEntry(".type").getString(null);
          switch (type) {
            case "ShuffleboardTab":
              System.out.println("Tab changed: " + path);
              tab.setProperties(properties(path));
              break;
            case "ShuffleboardLayout":
              System.out.println("Layout changed: " + path);
              LayoutModel layout = parent.getLayout(path, table.getEntry(".layout_type").getString(null));
              layout.setProperties(properties(path));
              parent = layout;
              break;
            default:
              System.out.println("Something else changed: " + path);
              System.out.println("  (type = " + type + ")");
              end = true;
              parent.getOrCreate(path, "Widget", preferredComponent(path, type), properties(path));
              break;
          }
        } else if (index > 1) {
          System.out.println("Something else changed: " + path);
          end = true;
          parent.getOrCreate(path, "Widget", preferredComponent(path, null), properties(path));
        }
        index++;
        if (end) {
          break;
        }
      }
      tabs.dirty();
    }, 0xFF);

    serverChangeListener.changed(null, null, serverId.get());
    serverId.addListener(serverChangeListener);
  }

  private NetworkTable metaTable(String dataTable) {
    return inst.getTable(dataTable.replaceFirst("^/Shuffleboard/", "/Shuffleboard/.metadata/"));
  }

  private String realPath(String metaPath) {
    return (metaPath.replaceFirst("^/Shuffleboard/.metadata/", "/Shuffleboard/"));
  }

  private String preferredComponent(String path, String fallback) {
    return metaTable(path).getEntry("PreferredComponent").getString(fallback);
  }

  private Map<String, Object> properties(String path) {
    NetworkTable table = metaTable(path);
    Map<String, Object> props = new LinkedHashMap<>();
    if (table.containsSubTable("Properties")) {
      NetworkTable propsTable = table.getSubTable("Properties");
      for (String k : propsTable.getKeys()) {
        props.put(k, propsTable.getEntry(k).getValue().getValue());
      }
    }
    return props;
  }

  @Override
  public void onUnload() {
    NetworkTablesJNI.removeEntryListener(recorderUid);
    NetworkTableUtils.shutdown(inst);
    serverId.removeListener(serverSaver);
  }

  @Override
  public List<ComponentType> getComponents() {
    return ImmutableList.of(
        WidgetType.forAnnotatedWidget(NetworkTableTreeWidget.class)
    );
  }

  @Override
  public List<SourceType> getSourceTypes() {
    return ImmutableList.of(
        NetworkTableSourceType.getInstance()
    );
  }

  @Override
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.of(
        DataTypes.Map, WidgetType.forAnnotatedWidget(NetworkTableTreeWidget.class)
    );
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Connection settings",
            Setting.of("Server",
                "The NetworkTables server to connect to. This can be a team number, IP address, or mDNS URL",
                new FlushableProperty<>(serverId)
            )
        )
    );
  }

  @Override
  public TabStructure getTabs() {
    return tabs;
  }

  public String getServerId() {
    return serverId.get();
  }

  public StringProperty serverIdProperty() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId.set(serverId);
  }

}
