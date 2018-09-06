package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.UiHints;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.FxUtils;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

@UiHints(showConnectionIndicator = false) // same host as NetworkTables plugin, so no need to duplicate that information
public final class CameraServerSourceType extends SourceType {

  public static final CameraServerSourceType INSTANCE = new CameraServerSourceType();

  private final Map<String, CameraServerSource> sources = new HashMap<>();
  private final ObservableList<String> availableUris = FXCollections.observableArrayList();
  private final ObservableMap<String, Object> availableSources = FXCollections.observableHashMap();

  private CameraServerSourceType() {
    super("CameraServer", true, "camera_server://", CameraServerSourceType::forName);
    NetworkTableInstance.getDefault().addEntryListener("/CameraPublisher", entryNotification ->
        FxUtils.runOnFxThread(() -> {
          List<String> hierarchy = NetworkTable.getHierarchy(entryNotification.name);
          // 0 is "/", 1 is "/CameraPublisher", 2 is "/CameraPublisher/<name>"
          String name = NetworkTable.basenameKey(hierarchy.get(2));
          String uri = toUri(name);
          NetworkTable table = NetworkTableInstance.getDefault().getTable(hierarchy.get(2));
          if (table.getKeys().isEmpty() && table.getSubTables().isEmpty()) {
            // No keys and no subtables, remove it
            availableUris.remove(uri);
            availableSources.remove(uri);
          } else if (!NetworkTableUtils.isDelete(entryNotification.flags)) {
            if (!availableUris.contains(uri)) {
              availableUris.add(uri);
            }
            availableSources.put(uri, new CameraServerData(name, null, 0, 0));
          }
        }), 0xFF);
  }

  public static CameraServerSource forName(String name) {
    return INSTANCE.sources.computeIfAbsent(name, CameraServerSource::new);
  }

  public static void removeSource(CameraServerSource source) {
    INSTANCE.sources.remove(source.getName());
  }

  @Override
  public void read(TimestampedData recordedData) {
    super.read(recordedData);
    CameraServerSource source = (CameraServerSource) forUri(recordedData.getSourceId());
    source.setData((CameraServerData) recordedData.getData());
  }

  @Override
  public void connect() {
    super.connect();
    sources.values().forEach(CameraServerSource::connect);
  }

  @Override
  public void disconnect() {
    sources.values().forEach(CameraServerSource::disconnect);
    super.disconnect();
  }

  @Override
  public SourceEntry createSourceEntryForUri(String uri) {
    return new CameraServerSourceEntry(removeProtocol(uri));
  }

  @Override
  public DataType<?> dataTypeForSource(DataTypes registry, String sourceUri) {
    return CameraServerDataType.Instance;
  }

  @Override
  public ObservableList<String> getAvailableSourceUris() {
    return availableUris;
  }

  @Override
  public ObservableMap<String, Object> getAvailableSources() {
    return availableSources;
  }

}
