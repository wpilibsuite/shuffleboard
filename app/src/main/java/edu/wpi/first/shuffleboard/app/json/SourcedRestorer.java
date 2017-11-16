package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

/**
 * Deals with destroyed sources and restoring those sources.
 */
public class SourcedRestorer {

  /**
   * Adds a {@link DestroyedSource} for each supported data type of a sourced object. The application will handle
   * restoring the sources when possible.
   *
   * @param sourced   the sourced object to add destroyed sources to
   * @param sourceUri the URI of the source
   */
  public void addDestroyedSourcesForAllDataTypes(Sourced sourced, String sourceUri) {
    sourced.getDataTypes().stream()
        .map(t -> DestroyedSource.forUnknownData(t, sourceUri))
        .forEach(source -> sourced.addSource((DataSource) source)); // ugly because javac can't figure the type
  }

}
