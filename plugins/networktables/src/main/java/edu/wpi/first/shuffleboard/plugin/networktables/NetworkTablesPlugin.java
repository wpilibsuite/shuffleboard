package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.DashboardMode;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.prefs.FlushableProperty;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.tab.model.TabStructure;
import edu.wpi.first.shuffleboard.api.util.PreferencesUtils;
import edu.wpi.first.shuffleboard.api.util.ShutdownHooks;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.WidgetType;
import edu.wpi.first.shuffleboard.plugin.networktables.sources.NetworkTableSourceType;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.NetworkTablesJNI;

import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;

@Description(
    group = "edu.wpi.first.shuffleboard",
    name = "NetworkTables",
    version = "2.2.5",
    summary = "Provides sources and widgets for NetworkTables"
)
public class NetworkTablesPlugin extends Plugin {

  private int recorderUid = -1;
  private final NetworkTableInstance inst = NetworkTableInstance.getDefault();
  private final Preferences preferences = Preferences.userNodeForPackage(getClass());

  private final StringProperty serverId = new SimpleStringProperty(this, "server", "localhost");
  private final InvalidationListener serverSaver = __ -> PreferencesUtils.save(serverId, preferences);

  private final TabGenerator tabGenerator = new TabGenerator(inst, Components.getDefault());
  private final RecorderController recorderController = RecorderController.createWithDefaultEntries(inst);

  private final ChangeListener<DashboardMode> dashboardModeChangeListener = (__, old, mode) -> {
    if (mode == DashboardMode.PLAYBACK) {
      recorderController.stop();
    } else {
      recorderController.start();
    }
  };

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

    NetworkTableUtils.shutdown(inst);
    if (value[0].matches("\\d{1,4}")) {
      inst.setServerTeam(Integer.parseInt(value[0]), port);
    } else if (value[0].isEmpty()) {
      inst.setServer("localhost", port);
    } else {
      inst.setServer(value[0], port);
    }
    inst.startClient();
    inst.startDSClient();
  };

  public NetworkTablesPlugin() {
    NetworkTableSourceType.setInstance(new NetworkTableSourceType(this));
  }

  @Override
  public void onLoad() {
    PreferencesUtils.read(serverId, preferences);
    serverId.addListener(serverSaver);

    // Automatically capture and record changes in network tables
    // This is done here because each key under N subtables would have N+1 copies
    // in the recording (eg "/a/b/c" has 2 tables and 3 copies: "/a", "/a/b", and "/a/b/c")
    // This significantly reduces the size of recording files.
    recorderUid = inst.addEntryListener("", event -> {
      Object value = event.value.getValue();
      DataTypes.getDefault().forJavaType(value.getClass())
          .ifPresent(type -> {
            Recorder.getInstance().record(
                NetworkTableSourceType.getInstance().toUri(event.name),
                type,
                value
            );
          });
    }, 0xFF);

    DashboardMode.currentModeProperty().addListener(dashboardModeChangeListener);
    recorderController.start();
    tabGenerator.start();

    serverChangeListener.changed(null, null, serverId.get());
    serverId.addListener(serverChangeListener);

    // Manually shut down the client to avoid a shutdown deadlock in ntcore. Don't want zombie processes eating up RAM
    // Note: this only seems to occur on Windows 7 machines
    ShutdownHooks.addHook(() -> {
      inst.stopClient();
      inst.stopDSClient();
    });
  }

  @Override
  public void onUnload() {
    DashboardMode.currentModeProperty().removeListener(dashboardModeChangeListener);
    recorderController.stop();
    tabGenerator.stop();
    NetworkTablesJNI.removeEntryListener(recorderUid);
    NetworkTableUtils.shutdown(inst);
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
        NetworkTableSourceType.getInstance()
    );
  }

  @Override
  public Map<DataType, ComponentType> getDefaultComponents() {
    return ImmutableMap.of(
        DataTypes.Map, WidgetType.forAnnotatedWidget(NetworkTableTreeWidget.class)
    );
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Connection settings",
            Setting.of("Server",
                "The NetworkTables server to connect to. This can be a team number, IP address, or mDNS URL",
                new FlushableProperty<>(serverId)
            )
        )
    );
  }

  @Override
  public TabStructure getTabs() {
    return tabGenerator.getStructure();
  }

  public String getServerId() {
    return serverId.get();
  }

  public StringProperty serverIdProperty() {
    return serverId;
  }

  public void setServerId(String serverId) {
    this.serverId.set(serverId);
  }

}
