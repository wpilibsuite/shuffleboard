package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;

/**
 * A data class representing the state of the application layout at the moment it is saved.
 */
public final class DashboardData {

  private final DashboardTabPane tabPane;
  private final WindowGeometry windowGeometry;

  /**
   * Creates a new dashboard data object.
   *
   * @param tabPane         the dashboard tab pane
   * @param windowGeometry  the geometry of the window
   */
  public DashboardData(DashboardTabPane tabPane, WindowGeometry windowGeometry) {
    this.tabPane = tabPane;
    this.windowGeometry = windowGeometry;
  }

  public DashboardTabPane getTabPane() {
    return tabPane;
  }

  public WindowGeometry getWindowGeometry() {
    return windowGeometry;
  }
}
