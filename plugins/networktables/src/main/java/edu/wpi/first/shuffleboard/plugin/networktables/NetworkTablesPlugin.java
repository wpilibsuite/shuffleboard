package edu.wpi.first.shuffleboard.plugin.networktables;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.prefs.FlushableProperty;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.util.PreferencesUtils;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;

import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javafx.beans.InvalidationListener;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

public class NetworkTablesPlugin extends Plugin {

  private int recorderUid = -1;
  private final NetworkTableInstance inst = NetworkTableInstance.getDefault();
  private final Preferences preferences = Preferences.userNodeForPackage(getClass());

  private final StringProperty serverId = new SimpleStringProperty(this, "server", "localhost");
  private final InvalidationListener serverSaver = __ -> PreferencesUtils.save(serverId, preferences);

  private final ChangeListener<String> serverChangeListener = (observable, oldValue, newValue) -> {
    String[] value = newValue.split(":");

    /*
     * You MUST set the port number before setting the team number.
     */
    int port;
    if (value.length > 1) {
      port = Integer.parseInt(value[1]);
    } else {
      port = NetworkTableInstance.kDefaultPort;
    }

    inst.stopClient();
    inst.stopDSClient();
    inst.deleteAllEntries();
    if (value[0].matches("\\d{1,4}")) {
      inst.setServerTeam(Integer.parseInt(value[0]), port);
    } else if (value[0].isEmpty()) {
      inst.setServer("localhost", port);
    } else {
      inst.setServer(value[0], port);
    }
    inst.startClient();
  };

  public NetworkTablesPlugin() {
    super("edu.wpi.first.shuffleboard", "NetworkTables", "1.0.0", "Provides sources and widgets for NetworkTables");
  }

  @Override
  public void onLoad() {
    PreferencesUtils.read(serverId, preferences);
    serverId.addListener(serverSaver);

    // Automatically capture and record changes in network tables
    // This is done here because each key under N subtables would have N+1 copies
    // in the recording (eg "/a/b/c" has 2 tables and 3 copies: "/a", "/a/b", and "/a/b/c")
    // This significantly reduces the size of recording files.
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

    inst.startClient();
    serverChangeListener.changed(null, null, serverId.get());
    serverId.addListener(serverChangeListener);
  }

  @Override
  public void onUnload() {
    NetworkTablesJNI.removeEntryListener(recorderUid);
    inst.stopClient();
    inst.stopDSClient();
    serverId.removeListener(serverSaver);
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

  @Override
  public List<Property<?>> getProperties() {
    return ImmutableList.of(
        // use FlushableProperty so changes made are only effected when committed by the user
        new FlushableProperty<>(serverId)
    );
  }

}
