package edu.wpi.first.shuffleboard.app;

import edu.wpi.first.shuffleboard.api.Populatable;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javafx.collections.ListChangeListener;

/**
 * An autopopulator is responsible for populating {@link Populatable Populatables} with components as sources are
 * discovered.
 */
public class Autopopulator {

  private final Set<Populatable> targets = new LinkedHashSet<>();
  private final SourceTypes sourceTypes;

  private static final Autopopulator defaultInstance = new Autopopulator(SourceTypes.getDefault());

  /**
   * Gets the default instance.
   */
  public static Autopopulator getDefault() {
    return defaultInstance;
  }

  /**
   * Creates a new autopopulator.
   *
   * @param sourceTypes the source type registry to use to find and create available sources
   */
  public Autopopulator(SourceTypes sourceTypes) {
    this.sourceTypes = sourceTypes;
    sourceTypes.allAvailableSourceUris().addListener(this::onChange);
  }

  private void onChange(ListChangeListener.Change<? extends String> change) {
    while (change.next()) {
      if (change.wasAdded()) {
        // Use a copy of the target list because addComponentIfPossible may add new targets,
        // causing a ConcurrentModificationException
        // Since addTarget also populates the target, we don't need to worry about responding to those changes here
        List<Populatable> currentTargets = new ArrayList<>(targets);
        change.getAddedSubList().forEach(id ->
            currentTargets.forEach(target -> target.addComponentIfPossible(SourceTypes.getDefault(), id)));
      }
    }
  }

  /**
   * Immediately populates a target with all known compatible sources.
   *
   * @param target the target to populate
   */
  public void populate(Populatable target) {
    sourceTypes.allAvailableSourceUris().forEach(uri -> target.addComponentIfPossible(SourceTypes.getDefault(), uri));
  }

  /**
   * Adds a population target. The target will be immediately populated with all compatible sources known at the time
   * this method is called, as well as be automatically populated with any sources discovered in the future.
   *
   * @param target a target to populate
   */
  public void addTarget(Populatable target) {
    Objects.requireNonNull(target, "target");
    targets.add(target);
    populate(target);
  }

  /**
   * Removes a population target. It will no longer be populated as new sources are discovered.
   *
   * @param target the target to remove
   */
  public void removeTarget(Populatable target) {
    targets.remove(target);
  }

}
