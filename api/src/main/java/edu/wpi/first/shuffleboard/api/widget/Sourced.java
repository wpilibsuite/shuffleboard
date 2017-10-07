package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;

/**
 * Common interface for objects that have data sources.
 */
public interface Sourced {

  /**
   * Gets the data source for this objects data.
   */
  DataSource<?> getSource();

  /**
   * Sets the source to use for this objects data.
   *
   * @param source the source to use
   *
   * @throws IncompatibleSourceException if the source is not compatible (for example, it has an unsupported data type)
   */
  void setSource(DataSource<?> source) throws IncompatibleSourceException;

}
