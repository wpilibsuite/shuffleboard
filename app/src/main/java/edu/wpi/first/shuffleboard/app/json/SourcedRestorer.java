package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Sourced;
import edu.wpi.first.shuffleboard.app.sources.DestroyedSource;

import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

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
    sourced.addSource(DestroyedSource.forUnknownData(sourced.getDataTypes(), sourceUri));
  }

  /**
   * Attempts to restore all destroyed sources in a sourced object for the given URIs.
   *
   * @param sourced       the sourced to restore sources for
   * @param urisToRestore the URIs of the sources to restore
   * @param errorHandler  a function to call when a source could not be restored, or when a restored source could not
   *                      be added
   */
  public void restoreSourcesFor(Sourced sourced,
                                Collection<? extends String> urisToRestore,
                                BiConsumer<DestroyedSource, ? super Throwable> errorHandler) {
    // The destroyed sources that correspond to URIs to restore
    List<DestroyedSource> toRestore = sourced.getSources().stream()
        .flatMap(TypeUtils.castStream(DestroyedSource.class))
        .filter(s -> urisToRestore.contains(s.getId()))
        .collect(Collectors.toList());
    for (DestroyedSource source : toRestore) {
      try {
        sourced.addSource(source.restore());
        // Remove all destroyed sources with the same ID; they were only present to allow us to restore the source
        // with the correct data type.  Since restoring this source was successful, the correct data type is known and
        // the remaining destroyed sources are no longer necessary
        sourced.getSources().removeIf(s -> s instanceof DestroyedSource && s.getId().equals(source.getId()));
      } catch (Throwable e) {
        errorHandler.accept(source, e);
      }
    }
  }

}
