package edu.wpi.first.shuffleboard.api.tab.model;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

public interface WidgetModel extends ComponentModel {

  /**
   * Gets the data source that the widget should use.
   */
  DataSource<?> getDataSource();

}
