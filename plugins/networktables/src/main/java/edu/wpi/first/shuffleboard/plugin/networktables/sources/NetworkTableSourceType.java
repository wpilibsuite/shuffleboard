package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.networktables.TopicInfo;
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
import edu.wpi.first.networktables.MultiSubscriber;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.PubSubOption;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public final class NetworkTableSourceType extends SourceType implements AutoCloseable {

  private static NetworkTableSourceType instance;

  private final ObservableList<String> availableSourceIds = FXCollections.observableArrayList();
  private final ObservableMap<String, Object> availableSources = FXCollections.observableHashMap();
  /** Maps source URIs to the last known data type. */
  private final Map<String, DataType> availableDataTypes = new HashMap<>();
  private final NetworkTablesPlugin plugin;
  private final MultiSubscriber subscriber;
  private final int topicListener;

  @SuppressWarnings("JavadocMethod")
  public NetworkTableSourceType(NetworkTablesPlugin plugin) {
    super("NetworkTables", true, "network_table://", NetworkTableSource::forKey);
    this.plugin = plugin;
    setConnectionStatus(new ConnectionStatus(plugin.getServerId(), false));
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    plugin.serverIdProperty().addListener((__, old, serverId) -> setConnectionStatus(serverId, false));
    inst.addConnectionListener(true,
        event -> setConnectionStatus(plugin.getServerId(), event.is(NetworkTableEvent.Kind.kConnected)));
    subscriber = new MultiSubscriber(inst, new String[] {""}, PubSubOption.topicsOnly(true));
    topicListener = inst.addListener(
        subscriber,
        EnumSet.of(
          NetworkTableEvent.Kind.kImmediate,
          NetworkTableEvent.Kind.kTopic),
        event -> {
          AsyncUtils.runAsync(() -> {
            final boolean delete = event.is(NetworkTableEvent.Kind.kUnpublish);
            final TopicInfo topicInfo = event.topicInfo;
            if (topicInfo.name.endsWith("/.type") && !delete) {
              // Got type metadata for composite data
              var compositeSourceId = toUri(topicInfo.name.substring(0, topicInfo.name.length() - 6));
              var topic = topicInfo.getTopic();
              var typeName = topic.getProperty("SmartDashboard");
              if ("null".equals(typeName)) {
                // Metadata property hasn't been set, fall back to use the generic map data type
                availableDataTypes.put(compositeSourceId, DataTypes.Map);
              } else {
                var dataType = DataTypes.getDefault().forName(typeName).orElse(DataTypes.Map);
                availableDataTypes.put(compositeSourceId, dataType);
              }
            }

            final String name = NetworkTableUtils.topicNameForEvent(event);
            List<String> hierarchy = NetworkTable.getHierarchy(name);
            for (int i = 0; i < hierarchy.size(); i++) {
              String uri = toUri(hierarchy.get(i));
              if (i == hierarchy.size() - 1) {
                // The full key
                if (delete) {
                  availableSources.remove(uri);
                  availableDataTypes.remove(uri);
                  Sources sources = Sources.getDefault();
                  sources.get(uri).ifPresent(sources::unregister);
                  NetworkTableSource.removeCachedSource(uri);
                } else {
                  var dataType = NetworkTableUtils.dataTypeForTypeString(topicInfo.typeStr);
                  availableSources.put(uri, dataType.getName());
                  availableDataTypes.put(uri, dataType);
                }
              }
              if (delete) {
                availableSourceIds.remove(uri);
                availableDataTypes.remove(uri);
              } else if (!availableSourceIds.contains(uri)) {
                availableSourceIds.add(uri);
              }
            }
          });
        });
  }

  @Override
  public void close() {
    subscriber.close();
    NetworkTableInstance.getDefault().removeListener(topicListener);
    availableSources.clear();
    availableDataTypes.clear();
    availableSourceIds.clear();
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
    FxUtils.runOnFxThread(() -> {
      // Add the data point to the set of available sources
      // Note: recorded data is the individual topics, not complex data
      if (!availableSourceIds.contains(recordedData.getSourceId())) {
        availableSourceIds.add(recordedData.getSourceId());
        availableSources.put(recordedData.getSourceId(), recordedData.getDataType().getName());
      }

      final String fullKey = removeProtocol(recordedData.getSourceId());
      NetworkTableEntry entry = NetworkTableInstance.getDefault().getEntry(fullKey);
      entry.setValue(recordedData.getData());
      if (!entry.getTopic().isRetained()) {
        entry.getTopic().setRetained(true);
      }
    });
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
    NetworkTableUtils.shutdown();
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
    return availableDataTypes.getOrDefault(sourceUri, DataTypes.Unknown);
  }

  @Override
  public String toUri(String sourceName) {
    return super.toUri(NetworkTable.normalizeKey(sourceName));
  }

}
