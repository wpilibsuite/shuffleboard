package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.DummySource;
import edu.wpi.first.shuffleboard.data.DataType;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public enum SourceType {

  NONE(false, "", null),
  STATIC(false, "example://", s -> DummySource.forTypes(DataType.forName(s)).get()),
  NETWORK_TABLE(true, "network_table://", NetworkTableSource::forKey),
  CAMERA_SERVER(true, "camera_server://", __ -> {throw new RuntimeException("Not Implemented");});

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

  public static DataSource<?> fromUri(String uri) {
    if (uri.isEmpty()) {
      return new EmptyDataSource();
    }
    String[] path = uri.split("://");
    String protocol = path[0] + "://";
    Optional<SourceType> type = Stream.of(values()).filter(t -> t.getProtocol().equals(protocol)).findFirst();

    return type
      .map(t -> t.sourceSupplier.apply(path[1]))
      .orElseThrow(() -> new RuntimeException("Couldn't find SourceType for protocol " + protocol));
  }
}
