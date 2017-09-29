package edu.wpi.first.shuffleboard.plugin.networktables;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.util.List;
import java.util.Map;

public class NetworkTablesPlugin extends Plugin {

  private int recorderUid = -1;

  public NetworkTablesPlugin() {
    super("edu.wpi.first.shuffleboard", "NetworkTables", "1.0.0", "Provides sources and widgets for NetworkTables");
  }

  @Override
  public void onLoad() {
    // Automatically capture and record changes in network tables
    // This is done here because each key under N subtables would have N+1 copies
    // in the recording (eg "/a/b/c" has 2 tables and 3 copies: "/a", "/a/b", and "/a/b/c")
    // This significantly reduces the size of recording files.
    recorderUid = NetworkTablesJNI.addEntryListener("", (uid, key, value, flags) -> {
      DataTypes.getDefault().forJavaType(value.getClass())
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
  public List<ComponentType> getComponents() {
    return ImmutableList.of(
        WidgetType.forAnnotatedWidget(NetworkTableTreeWidget.class)
    );
  }

  @Override
  public List<SourceType> getSourceTypes() {
    return ImmutableList.of(
        NetworkTableSourceType.INSTANCE
    );
  }

  @Override
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.of(
        DataTypes.Map, WidgetType.forAnnotatedWidget(NetworkTableTreeWidget.class)
    );
  }

}
