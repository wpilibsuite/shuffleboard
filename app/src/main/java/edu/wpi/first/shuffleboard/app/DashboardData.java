package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;

/**
 * A data class representing the state of the application layout at the moment it is saved.
 */
public final class DashboardData {

  private double dividerPosition;
  private DashboardTabPane tabPane;

  /**
   * Creates a new dashboard data object.
   *
   * @param dividerPosition the position of the divider between the dashboard tabs and the sources/gallery pane
   * @param tabPane         the dashboard tab pane
   */
  public DashboardData(double dividerPosition, DashboardTabPane tabPane) {
    this.dividerPosition = dividerPosition;
    this.tabPane = tabPane;
  }

  public double getDividerPosition() {
    return dividerPosition;
  }

  public void setDividerPosition(double dividerPosition) {
    this.dividerPosition = dividerPosition;
  }

  public DashboardTabPane getTabPane() {
    return tabPane;
  }

  public void setTabPane(DashboardTabPane tabPane) {
    this.tabPane = tabPane;
  }

}
