package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;

import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public final class NetworkTableSourceType extends SourceType {

  private static NetworkTableSourceType INSTANCE;

  private final ObservableList<String> availableSourceIds = FXCollections.observableArrayList();
  private final ObservableMap<String, Object> availableSources = FXCollections.observableHashMap();
  private final NetworkTablesPlugin plugin;

  @SuppressWarnings("JavadocMethod")
  public NetworkTableSourceType(NetworkTablesPlugin plugin) {
    super("NetworkTable", true, "network_table://", NetworkTableSource::forKey);
    this.plugin = plugin;
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    inst.addEntryListener("", (event) -> {
      AsyncUtils.runAsync(() -> {
        List<String> hierarchy = NetworkTableUtils.getHierarchy(event.name);
        for (int i = 0; i < hierarchy.size(); i++) {
          String uri = toUri(hierarchy.get(i));
          if (i == hierarchy.size() - 1) {
            availableSources.put(uri, event.value.getValue());
          }
          if (NetworkTableUtils.isDelete(event.flags)) {
            availableSourceIds.remove(uri);
          } else if (!availableSourceIds.contains(uri)) {
            availableSourceIds.add(uri);
          }
        }
      });
    }, 0xFF);
  }

  /**
   * For internal use only.
   */
  public static void setInstance(NetworkTableSourceType instance) {
    INSTANCE = instance;
  }

  public static NetworkTableSourceType getInstance() {
    return INSTANCE;
  }

  @Override
  public void read(TimestampedData recordedData) {
    super.read(recordedData);
    final String fullKey = removeProtocol(recordedData.getSourceId());
    NetworkTableEntry entry = NetworkTableInstance.getDefault().getEntry(fullKey);
    NetworkTableUtils.setEntryValue(entry, recordedData.getData());
  }

  @Override
  public void connect() {
    // force reconnect
    String id = plugin.getServerId();
    plugin.setServerId("");
    plugin.setServerId(id);
  }

  @Override
  public void disconnect() {
    NetworkTableUtils.shutdown(NetworkTableInstance.getDefault());
    availableSourceIds.clear();
    availableSources.clear();
  }

  @Override
  public ObservableList<String> getAvailableSourceUris() {
    return availableSourceIds;
  }

  @Override
  public ObservableMap<String, Object> getAvailableSources() {
    return availableSources;
  }

  @Override
  public SourceEntry createSourceEntryForUri(String uri) {
    return new NetworkTableSourceEntry(removeProtocol(uri), availableSources.get(uri));
  }

  @Override
  public String toUri(String sourceName) {
    return super.toUri(NetworkTableUtils.normalizeKey(sourceName));
  }

}
