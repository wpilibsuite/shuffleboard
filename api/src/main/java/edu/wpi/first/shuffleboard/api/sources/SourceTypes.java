package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.TimestampedData;
import edu.wpi.first.shuffleboard.api.util.PropertyUtils;
import edu.wpi.first.shuffleboard.api.util.Registry;

import org.fxmisc.easybind.EasyBind;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class SourceTypes extends Registry<SourceType> {

  // TODO replace with DI eg Guice
  private static SourceTypes defaultInstance = null;

  private final Map<String, SourceType> types = new HashMap<>();
  private final ObservableList<String> typeNames = FXCollections.observableArrayList();
  private final ObservableList<String> allUris = FXCollections.observableArrayList();

  public static final SourceType None = new NoneType();
  public static final SourceType Static = new StaticType();

  /**
   * Gets the default source type registry.
   */
  public static SourceTypes getDefault() {
    synchronized (SourceTypes.class) {
      if (defaultInstance == null) {
        defaultInstance = new SourceTypes();
      }
    }
    return defaultInstance;
  }

  /**
   * Creates a new source type registry.
   */
  public SourceTypes() {
    register(None);
    register(Static);

    typeNames.addListener((InvalidationListener) __ -> {
      Optional<ObservableList<String>> names = typeNames.stream()
          .map(this::forName)
          .map(SourceType::getAvailableSourceUris)
          .reduce(PropertyUtils::combineLists);
      names.ifPresent(l -> EasyBind.listBind(allUris, l));
    });
  }

  /**
   * Registers a new source type.
   *
   * @param sourceType the source type to register
   *
   * @throws IllegalArgumentException if a source type has already been registered with the same name
   * @throws IllegalArgumentException if a source type has already been registered with the same protocol
   */
  @Override
  public void register(SourceType sourceType) {
    Objects.requireNonNull(sourceType, "sourceType");
    if (isRegistered(sourceType)) {
      throw new IllegalArgumentException("Source type " + sourceType + " has already been registered");
    }
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
    addItem(sourceType);
  }

  /**
   * {@inheritDoc}
   *
   * @throws IllegalArgumentException if attempting to unregister {@link #None} or {@link #Static}
   */
  @Override
  public void unregister(SourceType sourceType) {
    if (None.equals(sourceType) || Static.equals(sourceType)) {
      throw new IllegalArgumentException("The default source types cannot be unregistered");
    }
    typeNames.remove(sourceType.getName());
    types.remove(sourceType.getName());
    removeItem(sourceType);
  }

  /**
   * Creates a data source corresponding to the given URI. If the protocol is not recognized, {@link DataSource#none()}
   * is returned.
   *
   * @param uri the URI to create a source for
   */
  public DataSource<?> forUri(String uri) {
    return typeForUri(uri).forUri(uri);
  }

  /**
   * Gets the source type with the given name, or {@link #None} if that name has not been registered.
   *
   * @param name the name of the source type to get
   */
  public SourceType forName(String name) {
    return types.getOrDefault(name, None);
  }

  /**
   * Gets the source type associated with the given URI, or {@link #None} if the protocol is not recognized.
   */
  public SourceType typeForUri(String uri) {
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
  public String stripProtocol(String uri) {
    return typeForUri(uri).removeProtocol(uri);
  }

  /**
   * Gets a read-only observable list of all available source URIs of all the known types.
   */
  public ObservableList<String> allAvailableSourceUris() {
    return allUris;
  }

  @UiHints(showConnectionIndicator = false)
  private static class NoneType extends SourceType {

    public NoneType() {
      super("None", false, "", __ -> DataSource.none());
    }

    @Override
    public DataType<?> dataTypeForSource(DataTypes registry, String sourceUri) {
      return DataTypes.None;
    }

    @Override
    public void read(TimestampedData recordedData) {
      // NOPE
    }
  }

  @UiHints(showConnectionIndicator = false)
  private static class StaticType extends SourceType {

    public StaticType() {
      super("Example", false, "example://", StaticType::sourceForUri);
    }

    @Override
    public DataType<?> dataTypeForSource(DataTypes registry, String sourceUri) {
      return registry.forName(removeProtocol(sourceUri)).orElse(DataTypes.Unknown);
    }

    private static DataSource sourceForUri(String uri) {
      return DummySource.forTypes(DataTypes.getDefault().forName(uri).orElse(DataTypes.Unknown))
          .orElseGet(DataSource::none);
    }

  }


}
