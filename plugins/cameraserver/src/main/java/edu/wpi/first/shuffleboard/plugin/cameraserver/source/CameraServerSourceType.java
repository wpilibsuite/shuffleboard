package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.util.List;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public final class CameraServerSourceType extends SourceType {

  public static final CameraServerSourceType INSTANCE = new CameraServerSourceType();

  private static final ObservableList<String> availableUris = FXCollections.observableArrayList();
  private static final ObservableMap<String, Object> availableSources = FXCollections.observableHashMap();

  private CameraServerSourceType() {
    // TODO fix bugs with recording before enabling it on master
    super("CameraServer", false, "camera_server://", CameraServerSource::forName);
    NetworkTablesJNI.addEntryListener("/CameraPublisher", (uid, key, value, flags) -> {
      Platform.runLater(() -> {
        List<String> hierarchy = NetworkTableUtils.getHierarchy(key);
        // 0 is "/", 1 is "/CameraPublisher", 2 is "/CameraPublisher/<name>"
        String name = NetworkTableUtils.simpleKey(hierarchy.get(2));
        String uri = toUri(name);
        if (CameraServerSource.rootTable.getSubTable(name).getKeys().isEmpty()
            && CameraServerSource.rootTable.getSubTable(name).getSubTables().isEmpty()) {
          // No keys and no subtables, remove it
          availableUris.remove(uri);
          availableSources.remove(uri);
        } else if (!NetworkTableUtils.isDelete(flags)) {
          if (!availableUris.contains(uri)) {
            availableUris.add(uri);
          }
          availableSources.put(uri, new CameraServerData(name, null));
        }
      });
    }, 0xFF);
  }

  @Override
  public void read(TimestampedData recordedData) {
    super.read(recordedData);
    CameraServerSource source = (CameraServerSource) forUri(recordedData.getSourceId());
    source.setData((CameraServerData) recordedData.getData());
  }

  @Override
  public SourceEntry createSourceEntryForUri(String uri) {
    return new CameraServerSourceEntry(new CameraServerData(removeProtocol(uri), null));
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
