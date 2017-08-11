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
  private final Function<String, DataSource> sourceSupplier;

  protected SourceType(String name,
                       boolean isRecordable,
                       String protocol,
                       Function<String, DataSource> sourceSupplier) {
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
    return sourceSupplier.apply(removeProtocol(uri));
  }

  /**
   * Gets a list of the URIs of all available sources of this type.
   */
  public ObservableList<String> getAvailableSourceUris() {
    return FXCollections.emptyObservableList();
  }

  public ObservableMap<String, Object> getAvailableSources() {
    return FXCollections.emptyObservableMap();
  }

  /**
   * Reads a data point and passes it to all appropriate sources of this type.The default
   * behavior is to do {
   * nothing;
   * }
   * recordable subclasses <i > must </i > override this method.
   */
  public void read(TimestampedData recordedData) {
    if (isRecordable) {
      throw new AbstractMethodError("A recordable source type must implement this method");
    }
  }

  public SourceEntry createSourceEntryForUri(String uri) {
    return null;
  }

}
