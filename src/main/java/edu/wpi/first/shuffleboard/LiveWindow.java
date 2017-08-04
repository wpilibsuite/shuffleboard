package edu.wpi.first.shuffleboard;

import com.google.common.annotations.VisibleForTesting;

import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

/**
 * Helper class for dealing with LiveWindow mode.
 */
public final class LiveWindow {

  @VisibleForTesting
  static ITable liveWindowTable = NetworkTable.getTable("LiveWindow");

  private static final BooleanProperty enabled = new SimpleBooleanProperty(LiveWindow.class, "enabled", false);

  static {
    liveWindowTable.getSubTable("~STATUS~").addTableListenerEx("LW Enabled", (source, key, value, isNew) -> {
      if (value instanceof Boolean) {
        enabled.set((Boolean) value);
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
