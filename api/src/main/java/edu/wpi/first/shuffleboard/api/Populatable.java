package edu.wpi.first.shuffleboard.api;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

/**
 * Common interface for items that support auto-population of UI components by the application.
 */
public interface Populatable {

  /**
   * Checks if the given data source is supported, ie it matches some criteria; for example, a widget that populates
   * with data under a certain path should check if the source's ID starts with that path. Containers with no criteria
   * should always return {@code true}.
   *
   * @param source the source to check
   */
  boolean supports(DataSource<?> source);

  /**
   * Checks if this contains a component corresponding to a data source.
   *
   * @param source the source to check components for
   */
  boolean hasComponentFor(DataSource<?> source);

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
    if (supports(source) && !hasComponentFor(source)) {
      addComponentFor(source);
    }
  }

}
