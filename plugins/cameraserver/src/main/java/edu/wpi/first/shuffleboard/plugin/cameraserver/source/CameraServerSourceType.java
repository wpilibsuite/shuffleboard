package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class CameraServerSourceType extends SourceType {

  public static final CameraServerSourceType INSTANCE = new CameraServerSourceType();

  private static final ObservableList<String> availableUris = FXCollections.observableArrayList();
  private static final ObservableMap<String, Object> availableSources = FXCollections.observableHashMap();

  private CameraServerSourceType() {
    super("CameraServer", false, "camera_server://", name -> null);
    NetworkTablesJNI.addEntryListener("/CameraServer", (uid, key, value, flags) -> {
      List<String> hierarchy = NetworkTableUtils.getHierarchy(key);
      // 0 is "/", 1 is "/CameraServer", 2 is "/CameraServer/<name>"
      String name = NetworkTableUtils.simpleKey(hierarchy.get(2));
      String uri = toUri(name);
      if (NetworkTable.getTable(hierarchy.get(2)).getKeys().isEmpty()) {
        availableUris.remove(uri);
        availableSources.remove(uri);
      } else if (!NetworkTableUtils.isDelete(flags)) {
        if (!availableUris.contains(uri)) {
          availableUris.add(uri);
        }
        availableSources.put(uri, new CameraServerData(name, null));
      }
    }, 0xFF);
  }

  @Override
  public SourceEntry createSourceEntryForUri(String uri) {
    return new CameraServerSourceEntry(null);
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
