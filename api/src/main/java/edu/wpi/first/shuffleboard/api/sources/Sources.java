package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.util.Registry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Keeps track of all created data sources. This is primarily used for data recording and playback.
 */
public class Sources extends Registry<DataSource> {

  // TODO replace with DI eg Guice
  private static final Sources defaultInstance = new Sources();

  private final Map<String, DataSource> sources = new HashMap<>();

  public static Sources getDefault() {
    return defaultInstance;
  }

  @Override
  public void register(DataSource source) {
    Objects.requireNonNull(source, "source");
    if (isRegistered(source)) {
      throw new IllegalArgumentException("Source " + source + " has already been registered");
    }
    sources.put(source.getId(), source);
    addItem(source);
  }

  @Override
  public void unregister(DataSource source) {
    sources.remove(source.getId());
    removeItem(source);
  }

  /**
   * Gets a list of all the known data sources.
   *
   * @param type the source type to get the sources for
   */
  public List<DataSource> forType(SourceType type) {
    return sources.values().stream()
        .filter(s -> s.getType().equals(type))
        .collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked") //NOPMD multiple occurrences of string literal
  public <T> Optional<DataSource<T>> get(String id) {
    return Optional.ofNullable(sources.get(id));
  }

  @SuppressWarnings("unchecked") //NOPMD multiple occurrences of string literal
  public <T> DataSource<T> computeIfAbsent(String uri, Supplier<DataSource<T>> sourceSupplier) {
    return sources.computeIfAbsent(uri, __ -> sourceSupplier.get());
  }

  public Stream<DataSource<?>> hierarchy(DataSource<?> source) {
    return DataSourceUtils.getHierarchy(source.getName()).stream()
        .map(n -> source.getType().forUri(source.getType().toUri(n)));
  }

}
