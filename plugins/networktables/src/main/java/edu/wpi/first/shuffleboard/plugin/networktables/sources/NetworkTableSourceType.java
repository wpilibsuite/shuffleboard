package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.ConnectionStatus;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.NetworkTablesPlugin;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public final class NetworkTableSourceType extends SourceType {

  private static NetworkTableSourceType instance;

  private final ObservableList<String> availableSourceIds = FXCollections.observableArrayList();
  private final ObservableMap<String, Object> availableSources = FXCollections.observableHashMap();
  private final NetworkTablesPlugin plugin;

  @SuppressWarnings("JavadocMethod")
  public NetworkTableSourceType(NetworkTablesPlugin plugin) {
    super("NetworkTables", true, "network_table://", NetworkTableSource::forKey);
    this.plugin = plugin;
    setConnectionStatus(new ConnectionStatus(plugin.getServerId(), false));
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    plugin.serverIdProperty().addListener((__, old, serverId) -> setConnectionStatus(serverId, false));
    inst.addConnectionListener(notification -> setConnectionStatus(plugin.getServerId(), notification.connected), true);
    inst.addConnectionListener(notification -> {
      if (!notification.connected) {
        FxUtils.runOnFxThread(() -> {
          availableSources.clear();
          availableSourceIds.clear();
          NetworkTableSource.removeAllCachedSources();
        });
      }
    }, false);
    inst.addEntryListener("", event -> {
      AsyncUtils.runAsync(() -> {
        final boolean delete = NetworkTableUtils.isDelete(event.flags);
        List<String> hierarchy = NetworkTable.getHierarchy(event.name);
        for (int i = 0; i < hierarchy.size(); i++) {
          String uri = toUri(hierarchy.get(i));
          if (i == hierarchy.size() - 1) {
            if (delete) {
              availableSources.remove(uri);
              Sources sources = Sources.getDefault();
              sources.get(uri).ifPresent(sources::unregister);
              NetworkTableSource.removeCachedSource(uri);
            } else {
              availableSources.put(uri, event.value.getValue());
            }
          }
          if (delete) {
            availableSourceIds.remove(uri);
          } else if (!availableSourceIds.contains(uri)) {
            availableSourceIds.add(uri);
          }
        }
      });
    }, 0xFF);
  }

  private void setConnectionStatus(String serverId, boolean connected) {
    Platform.runLater(() -> {
      String host;
      if (serverId.isEmpty()) {
        // empty server ID is treated as localhost by the plugin, so display it accordingly
        host = "localhost";
      } else {
        host = serverId;
      }
      setConnectionStatus(new ConnectionStatus(host, connected));
    });
  }

  /**
   * For internal use only.
   */
  public static void setInstance(NetworkTableSourceType instance) {
    NetworkTableSourceType.instance = instance;
  }

  public static NetworkTableSourceType getInstance() {
    return instance;
  }

  @Override
  public void read(TimestampedData recordedData) {
    super.read(recordedData);
    final String fullKey = removeProtocol(recordedData.getSourceId());
    NetworkTableEntry entry = NetworkTableInstance.getDefault().getEntry(fullKey);
    entry.setValue(recordedData.getData());
  }

  @Override
  public void connect() {
    // force reconnect
    // This is ugly and I hate it, but it works
    String id = plugin.getServerId();
    plugin.setServerId("notarealserver:0");
    plugin.setServerId(id);
    super.setConnectionStatus(new ConnectionStatus(id, false));
  }

  @Override
  public void disconnect() {
    NetworkTableUtils.shutdown(NetworkTableInstance.getDefault());
    availableSourceIds.clear();
    availableSources.clear();
    super.disconnect();
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
  public DataType<?> dataTypeForSource(DataTypes registry, String sourceUri) {
    return NetworkTableUtils.dataTypeForEntry(removeProtocol(sourceUri));
  }

  @Override
  public String toUri(String sourceName) {
    return super.toUri(NetworkTable.normalizeKey(sourceName));
  }

}
