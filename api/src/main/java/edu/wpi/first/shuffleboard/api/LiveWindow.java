package edu.wpi.first.shuffleboard.api;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.TableEntryListener;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Helper class for dealing with LiveWindow mode.
 */
public final class LiveWindow {

  private static final NetworkTableInstance inst = NetworkTableInstance.getDefault();
  private static final NetworkTable liveWindowTable = inst.getTable("LiveWindow");

  private static final BooleanProperty enabled = new SimpleBooleanProperty(LiveWindow.class, "enabled", false);

  private static final TableEntryListener enabledListener = (table, source, key, value, flags) -> {
    if (value.isBoolean()) {
      enabled.set(value.getBoolean());
    } else {
      throw new IllegalArgumentException("The key 'LW Enabled' must be a boolean (was " + value + ")");
    }
  };

  static {
    liveWindowTable.getSubTable(".status").addEntryListener("LW Enabled", enabledListener, 0xFF);

    // for backwards compatibility with pre-2018 programs
    liveWindowTable.getSubTable("~STATUS~").addEntryListener("LW Enabled", enabledListener, 0xFF);
  }

  private LiveWindow() {
  }

  public static ReadOnlyBooleanProperty enabledProperty() {
    return enabled;
  }

  public static boolean isEnabled() {
    return enabled.get();
  }

}
