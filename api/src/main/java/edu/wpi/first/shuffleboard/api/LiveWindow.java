package edu.wpi.first.shuffleboard.api;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

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

  static {
    liveWindowTable.getSubTable("~STATUS~").addEntryListener("LW Enabled", (table, source, key, value, flags) -> {
      if (value.isBoolean()) {
        enabled.set(value.getBoolean());
      } else {
        throw new IllegalArgumentException("The key 'LW Enabled' must be a boolean (was " + value + ")");
      }
    }, 0xFF);
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
