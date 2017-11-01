package edu.wpi.first.shuffleboard.api;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

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
      = new SimpleObjectProperty<>(DashboardMode.class, "currentMode", NORMAL);

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

}
