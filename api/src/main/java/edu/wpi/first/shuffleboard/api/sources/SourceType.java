package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;

import java.util.function.Function;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

public class SourceType {

  private final String name;
  private final boolean isRecordable;
  private final String protocol;
  private final Function<String, DataSource<?>> sourceSupplier;

  /**
   * Creates a new source type.
   *
   * @param name           the name of the new source type. <i>This must be unique among all source types.</i>
   * @param isRecordable   if sources of this type may have their values recorded
   * @param protocol       the protocol string for source URIs using this source type. For example, 'network_table://"
   * @param sourceSupplier a function to use to create data sources of this type for a given name (<i>not</i> URI)
   */
  public SourceType(String name,
                    boolean isRecordable,
                    String protocol,
                    Function<String, DataSource<?>> sourceSupplier) {
    this.name = name;
    this.isRecordable = isRecordable;
    this.protocol = protocol;
    this.sourceSupplier = sourceSupplier;
  }

  public final String getName() {
    return name;
  }

  public final boolean isRecordable() {
    return isRecordable;
  }

  public String getProtocol() {
    return protocol;
  }

  public String toUri(String sourceName) {
    return protocol + sourceName;
  }

  /**
   * Removes the protocol prefix from a string. Has no effect if the given text does not start with this types protocol.
   *
   * @param text the text to remove the protocol string from
   */
  public String removeProtocol(String text) {
    if (text.startsWith(protocol)) {
      return text.substring(protocol.length());
    } else {
      return text;
    }
  }

  /**
   * Given a URI-like string with a protocol and a pseudo-path, return a source for the current SourceType
   * This function will throw an error if it doesn't support the protocol that's passed.
   *
   * <p>The interpretation of paths may vary depending on any given SourceType.</p>
   */
  public DataSource<?> forUri(String uri) {
    if (!uri.startsWith(protocol)) {
      throw new IllegalArgumentException("URI does not start with the correct protocol: " + uri);
    }
    return Sources.getDefault().computeIfAbsent(uri, () -> sourceSupplier.apply(removeProtocol(uri)));
  }

  /**
   * Gets a list of the URIs of all available sources of this type.
   */
  public ObservableList<String> getAvailableSourceUris() {
    return FXCollections.emptyObservableList();
  }

  /**
   * Gets a observable map of available source URIs to their values.
   */
  public ObservableMap<String, Object> getAvailableSources() {
    return FXCollections.emptyObservableMap();
  }

  /**
   * Reads a data point and passes it to all appropriate sources of this type.The default
   * behavior is to do nothing; recordable subclasses <i> must </i> override this method.
   */
  public void read(TimestampedData recordedData) {
    getAvailableSourceUris().add(recordedData.getSourceId());
    getAvailableSources().put(recordedData.getSourceId(), recordedData.getData());
  }

  /**
   * Creates a root source entry. The entry will not be used to create a source, but to represent the root node in
   * the source tree view in the application window.
   */
  public SourceEntry createRootSourceEntry() {
    return createSourceEntryForUri("/");
  }

  /**
   * Creates a source entry corresponding to the given URI. The default implementation throws an exception; custom
   * source types should override this behavior.
   *
   * @param uri the source URI to create a source entry for
   *
   * @throws UnsupportedOperationException if this method has not been overridden by a subclass.
   */
  public SourceEntry createSourceEntryForUri(String uri) {
    throw new UnsupportedOperationException("Not implemented by " + getName());
  }

}
