package edu.wpi.first.shuffleboard.plugin.networktables;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.types.MapType;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;

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
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    recorderUid = inst.addEntryListener("", (event) -> {
      Object value = event.value.getValue();
      DataTypes.getDefault().forJavaType(value.getClass())
          .ifPresent(type -> {
            Recorder.getInstance().record(
                NetworkTableSourceType.INSTANCE.toUri(event.name),
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

  @Override
  public Map<DataType, Class<? extends Widget>> getDefaultWidgets() {
    return ImmutableMap.of(
        new MapType(), NetworkTableTreeWidget.class
    );
  }

}
