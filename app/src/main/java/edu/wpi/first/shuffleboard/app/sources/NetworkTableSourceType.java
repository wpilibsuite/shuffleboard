package edu.wpi.first.shuffleboard.app.sources;

import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class NetworkTableSourceType extends SourceType {

  public static final NetworkTableSourceType INSTANCE = new NetworkTableSourceType();

  static {
    SourceTypes.register(INSTANCE);
  }

  private final ObservableList<String> availableSourceIds = FXCollections.observableArrayList();

  private NetworkTableSourceType() {
    super("NetworkTable", true, "network_table://", NetworkTableSource::forKey);
    NetworkTablesJNI.addEntryListener("", (uid, key, value, flags) -> {
      NetworkTableUtils.getHierarchy(key)
          .stream()
          .map(this::toUri)
          .forEach(uri -> {
            if (NetworkTableUtils.isDelete(flags)) {
              availableSourceIds.remove(uri);
            } else if (!availableSourceIds.contains(uri)) {
              availableSourceIds.add(uri);
            }
          });
    }, 0xFF);
  }

  @Override
  public ObservableList<String> getAvailableSourceIds() {
    return availableSourceIds;
  }

}
