package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.DummySource;
import edu.wpi.first.shuffleboard.data.DataType;

import java.util.function.Function;
import java.util.stream.Stream;

public enum SourceType {

  NONE(false, "", null),
  STATIC(false, "example://", s -> DummySource.forTypes(DataType.forName(s)).get()),
  NETWORK_TABLE(true, "network_table://", NetworkTableSource::forKey),
  CAMERA_SERVER(true, "camera_server://", __ -> {
    throw new UnsupportedOperationException("Not implemented");
  });

  public final boolean isRecordable;
  private final String protocol;
  private final Function<String, DataSource> sourceSupplier;

  SourceType(boolean isRecordable, String protocol, Function<String, DataSource> sourceSupplier) {
    this.isRecordable = isRecordable;
    this.protocol = protocol;
    this.sourceSupplier = sourceSupplier;
  }

  public String toUri(String sourceName) {
    return protocol + sourceName;
  }

  public String getProtocol() {
    return protocol;
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
   * Given a URI-like string with a protocol and a pseudo-path, return a source for that protocol.
   */
  public static DataSource<?> fromUri(String uri) {
    return Stream.of(values())
            .filter(type -> type != NONE && uri.startsWith(type.protocol))
            .findFirst()
            .map(type -> type.forUri(uri))
            .orElseThrow(() -> new RuntimeException("Couldn't find SourceType for " + uri));
  }
}
