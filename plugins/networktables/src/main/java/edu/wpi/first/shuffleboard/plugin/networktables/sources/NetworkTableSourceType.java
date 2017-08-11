package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public final class NetworkTableSourceType extends SourceType {

  public static final NetworkTableSourceType INSTANCE = new NetworkTableSourceType();

  private final ObservableList<String> availableSourceIds = FXCollections.observableArrayList();
  private final ObservableMap<String, Object> availableSources = FXCollections.observableHashMap();

  private NetworkTableSourceType() {
    super("NetworkTable", true, "network_table://", NetworkTableSource::forKey);
    NetworkTablesJNI.addEntryListener("", (uid, key, value, flags) -> {
      AsyncUtils.runAsync(() -> {
        NetworkTableUtils.getHierarchy(key)
            .stream()
            .map(this::toUri)
            .forEach(uri -> {
              availableSources.put(uri, value);
              if (NetworkTableUtils.isDelete(flags)) {
                availableSourceIds.remove(uri);
              } else if (!availableSourceIds.contains(uri)) {
                availableSourceIds.add(uri);
              }
            });
      });
    }, 0xFF);
  }

  @Override
  public void read(TimestampedData recordedData) {
    // Update all possible sources for the entry
    // This is a special case because of the treelike structure of network tables
    final String fullKey = NetworkTableSourceType.INSTANCE.removeProtocol(recordedData.getSourceId());
    List<String> hierarchy = NetworkTableUtils.getHierarchy(fullKey);
    hierarchy.stream()
        .map(NetworkTableSourceType.INSTANCE::toUri)
        .map(Sources::get)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(source -> {
          if (source instanceof CompositeNetworkTableSource) {
            @SuppressWarnings("unchecked")
            CompositeNetworkTableSource<? extends ComplexData<?>> comp = (CompositeNetworkTableSource) source;
            if (comp.getKey().equals("/")) {
              updateTable(comp, fullKey, recordedData.getData());
            } else {
              updateTable(comp, fullKey.substring(comp.getKey().length() + 1), recordedData.getData());
            }
          } else {
            // It's the source just for the key, set it
            source.setData(recordedData.getData());
          }
        });
  }

  private <T extends ComplexData<T>> void updateTable(CompositeNetworkTableSource<T> source, String key, Object value) {
    Map<String, Object> map = new HashMap<>(source.getData().asMap());
    map.put(key, value);
    source.setData(source.getDataType().fromMap(map));
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
    String key = removeProtocol(uri);
    return new NetworkTableEntry(key, NetworkTablesJNI.getValue(key, null));
  }

  @Override
  public String toUri(String sourceName) {
    return super.toUri(NetworkTableUtils.normalizeKey(sourceName));
  }

}
