package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.util.PropertyUtils;

import org.fxmisc.easybind.EasyBind;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class SourceTypes {

  private static final Map<String, SourceType> types = new HashMap<>();
  private static final ObservableList<String> typeNames = FXCollections.observableArrayList();
  private static final ObservableList<String> allUris = FXCollections.observableArrayList();


  public static final SourceType None = new SourceType("None", false, "", __ -> DataSource.none());
  public static final SourceType Static
      = new SourceType("Static", false, "example://", uri -> DummySource.forTypes(DataTypes.forName(uri).get()).get());

  static {
    typeNames.addListener((InvalidationListener) __ -> {
      Optional<ObservableList<String>> names = typeNames.stream()
          .map(SourceTypes::forName)
          .map(SourceType::getAvailableSourceUris)
          .reduce(PropertyUtils::combineLists);
      names.ifPresent(l -> EasyBind.listBind(allUris, l));
    });

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
    typeNames.add(name);
  }

  /**
   * Creates a data source corresponding to the given URI. If the protocol is not recognized, {@link DataSource#none()}
   * is returned.
   *
   * @param uri the URI to create a source for
   */
  public static DataSource<?> forUri(String uri) {
    return typeForUri(uri).forUri(uri);
  }

  /**
   * Gets the source type with the given name, or {@link #None} if that name has not been registered.
   *
   * @param name the name of the source type to get
   */
  public static SourceType forName(String name) {
    return types.getOrDefault(name, None);
  }

  /**
   * <<<<<<< d13462fe4829f201f7c4203580dcd7a745f35204
   * Gets the source type associated with the given URI, or {@link #None} if the protocol is not recognized.
   */
  public static SourceType typeForUri(String uri) {
    for (SourceType type : types.values()) {
      if (!type.getProtocol().isEmpty() && uri.startsWith(type.getProtocol())) {
        return type;
      }
    }
    return None;
  }

  /**
   * Tries to strip the protocol from a source URI. Has no effect if the uri does not start with a known protocol.
   *
   * @param uri the uri to strip the protocol from
   */
  public static String stripProtocol(String uri) {
    return typeForUri(uri).removeProtocol(uri);
  }

  /**
   * Gets a read-only observable list of all available source URIs of all the known types.
   */
  public static ObservableList<String> allAvailableSourceUris() {
    return allUris;
  }

}
