package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.DummySource;
import edu.wpi.first.shuffleboard.data.DataType;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public enum SourceType {

  NONE(false, "", null) {
    @Override
    public ObservableList<String> getAvailableSourceIds() {
      return FXCollections.emptyObservableList();
    }
  },
  STATIC(false, "example://", s -> DummySource.forTypes(DataType.forName(s)).get()) {
    @Override
    public ObservableList<String> getAvailableSourceIds() {
      return FXCollections.emptyObservableList();
    }
  },
  NETWORK_TABLE(true, "network_table://", NetworkTableSource::forKey) {

    private final ObservableList<String> ids = FXCollections.observableArrayList();

    {
      NetworkTablesJNI.addEntryListener("", (uid, key, value, flags) -> {
        List<String> hierarchy = NetworkTableUtils.getHierarchy(key);
        if (NetworkTableUtils.isDelete(flags)) {
          hierarchy.stream()
              .map(this::toUri)
              .forEach(ids::remove);
        } else {
          hierarchy.stream()
              .map(this::toUri)
              .filter(uri -> !ids.contains(uri))
              .forEach(ids::add);
        }
      }, 0xFF);
    }

    @Override
    public ObservableList<String> getAvailableSourceIds() {
      return ids;
    }
  },
  CAMERA_SERVER(true, "camera_server://", __ -> {
    throw new UnsupportedOperationException("Not implemented");
  }) {
    @Override
    public ObservableList<String> getAvailableSourceIds() {
      return FXCollections.emptyObservableList();
    }
  };

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

  public static SourceType typeForUri(String uri) {
    for (SourceType type : values()) {
      if (!type.protocol.isEmpty() && uri.startsWith(type.protocol)) {
        return type;
      }
    }
    return NONE;
  }

  /**
   * Tries to strip the protocol from a source URI. Has no affect if the uri does not start with a known protocol.
   *
   * @param uri the uri to strip the protocol from
   */
  public static String stripProtocol(String uri) {
    return typeForUri(uri).removeProtocol(uri);
  }

  /**
   * Gets a list of the IDs of all available sources of this type.
   */
  public abstract ObservableList<String> getAvailableSourceIds();

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
