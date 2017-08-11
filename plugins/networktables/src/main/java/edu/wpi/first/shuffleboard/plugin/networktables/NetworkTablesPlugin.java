package edu.wpi.first.shuffleboard.plugin.networktables;

import com.google.common.collect.ImmutableList;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.util.List;

public class NetworkTablesPlugin extends Plugin {

  private int recorderUid = -1;

  public NetworkTablesPlugin() {
    super("NetworkTables");
  }

  @Override
  public void onLoad() {
    // Automatically capture and record changes in network tables
    // This is done here because each key under N subtables would have N+1 copies
    // in the recording (eg "/a/b/c" has 2 tables and 3 copies: "/a", "/a/b", and "/a/b/c")
    // This significantly reduces the size of recording files.
    recorderUid = NetworkTablesJNI.addEntryListener("", (uid, key, value, flags) -> {
      DataTypes.forJavaType(value.getClass())
          .ifPresent(type -> {
            Recorder.getInstance().record(
                NetworkTableSourceType.INSTANCE.toUri(key),
                type,
                value
            );
          });
    }, 0xFF);
  }

  @Override
  public void onUnload() {
    NetworkTablesJNI.removeEntryListener(recorderUid);
  }

  @Override
  public List<Class<? extends Widget>> getWidgets() {
    return ImmutableList.of(
        NetworkTableTreeWidget.class
    );
  }

  @Override
  public List<SourceType> getSourceTypes() {
    return ImmutableList.of(
        NetworkTableSourceType.INSTANCE
    );
  }

}
