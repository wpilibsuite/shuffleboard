package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.data.DataType;

import java.util.HashMap;
import java.util.Map;

public final class SourceTypes {

  private static final Map<String, SourceType> types = new HashMap<>();

  public static final SourceType None = new SourceType("None", false, "", null);
  public static final SourceType Static
      = new SourceType("Static", false, "example://", uri -> DummySource.forTypes(DataType.forName(uri)).get());

  static {
    register(None);
    register(Static);
  }

  private SourceTypes() {

  }

  /**
   * Registers a new source type.
   *
   * @param sourceType the source type to register
   *
   * @throws IllegalArgumentException if a source type has already been registered with the same name
   * @throws IllegalArgumentException if a source type has already been registered with the same protocol
   */
  public static void register(SourceType sourceType) {
    String name = sourceType.getName();
    if (types.containsKey(name)) {
      throw new IllegalArgumentException(
          "A source type has already been registered with name '" + name + "': " + types.get(name));
    }

    String protocol = sourceType.getProtocol();
    if (types.values().stream().anyMatch(t -> t.getProtocol().equals(protocol))) {
      throw new IllegalArgumentException("A source type has already been registered with protocol '" + protocol + "'");
    }
    types.put(name, sourceType);
  }

  /**
   * Creates a data source corresponding to the given URI. If the protocol is not recognized, {@link DataSource#none()}
   * is returned.
   *
   * @param uri the URI to create a source for
   */
  public static DataSource<?> forUri(String uri) {
    return types.values().stream()
        .filter(t -> t != None)
        .filter(t -> uri.startsWith(t.getProtocol()))
        .map(t -> t.forUri(uri))
        .findFirst()
        .orElseGet(DataSource::none);
  }

  /**
   * Gets the source type with the given name, or {@link #None} if that name has not been registered.
   *
   * @param name the name of the source type to get
   */
  public static SourceType forName(String name) {
    return types.getOrDefault(name, None);
  }

}
