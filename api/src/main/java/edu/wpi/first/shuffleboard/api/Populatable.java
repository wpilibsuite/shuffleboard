package edu.wpi.first.shuffleboard.api;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;

/**
 * Common interface for items that support auto-population of UI components by the application.
 */
public interface Populatable {

  /**
   * Checks if the given data source is supported, ie it matches some criteria; for example, a widget that populates
   * with data under a certain path should check if the source's ID starts with that path. Containers with no criteria
   * should always return {@code true}.
   *
   * @param sourceId the ID of the source to check
   */
  boolean supports(String sourceId);

  /**
   * Checks if this contains a component corresponding to a data source.
   *
   * @param sourceId the ID of the source to check components for
   */
  boolean hasComponentFor(String sourceId);

  /**
   * Adds a component for a data source. This should always add a component;
   *
   * @param source the source to add a component for
   */
  void addComponentFor(DataSource<?> source);

  /**
   * If possible, adds a component for a data source. A component will only be added iff the source is supported and
   * there is no component present that already corresponds to that source.
   *
   * @param source the source to check/add a component for
   */
  default void addComponentIfPossible(DataSource<?> source) {
    if (supports(source.getId()) && !hasComponentFor(source.getId())) {
      addComponentFor(source);
    }
  }

  /**
   * If possible, adds a component for a data source. A component will only be added iff the source ID is supported and
   * there is no component present that already corresponds to that source ID.
   *
   * @param sourceTypes the source type registry to use to create a new source to populate
   * @param sourceId the ID of the source for the component to add
   */
  default void addComponentIfPossible(SourceTypes sourceTypes, String sourceId) {
    if (supports(sourceId) && !hasComponentFor(sourceId)) {
      addComponentFor(sourceTypes.forUri(sourceId));
    }
  }

}
