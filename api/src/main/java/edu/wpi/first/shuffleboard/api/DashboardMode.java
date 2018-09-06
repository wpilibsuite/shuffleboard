package edu.wpi.first.shuffleboard.api;

import edu.wpi.first.shuffleboard.api.properties.AsyncProperty;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;

/**
 * An enum representing the possible modes of dashboard operation.
 */
public enum DashboardMode {

  /**
   * Normal operation. No recording or playback is taking place.
   */
  NORMAL,

  /**
   * The dashboard behaves as {@link #NORMAL}, but records all incoming data in the background.
   */
  RECORDING,

  /**
   * When in this mode, the dashboard is playing back recorded data.
   */
  PLAYBACK;

  private static final ObjectProperty<DashboardMode> currentMode
      = new AsyncProperty<>(DashboardMode.class, "currentMode", NORMAL);

  public static Property<DashboardMode> currentModeProperty() {
    return currentMode;
  }

  /**
   * Gets the current mode of the dashboard.
   */
  public static DashboardMode getCurrentMode() {
    return currentMode.get();
  }

  /**
   * Sets the current mode of the dashboard.
   */
  public static void setCurrentMode(DashboardMode currentMode) {
    DashboardMode.currentMode.set(currentMode);
  }

  public static boolean inPlayback() {
    return getCurrentMode() == PLAYBACK;
  }

}
