package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

import javafx.collections.ObservableList;

/**
 * Common interface for objects that have data sources.
 */
public interface Sourced {

  /**
   * Adds a source.
   *
   * @param source the source to add
   *
   * @throws IncompatibleSourceException if the source is not compatible with this object
   */
  void addSource(DataSource source) throws IncompatibleSourceException;

  /**
   * Gets an observable list of the sources for this object.
   */
  ObservableList<DataSource> getSources();

  default void removeSource(DataSource source) {
    getSources().remove(source);
    source.removeClient(this);
  }

  default void removeAllSources() {
    getSources().forEach(s -> s.removeClient(this));
    getSources().clear();
  }

  /**
   * Gets the allowable data types for sources. Defaults to {@link DataTypes#All}.
   */
  default Set<DataType> getDataTypes() {
    return ImmutableSet.of(DataTypes.All);
  }

}
