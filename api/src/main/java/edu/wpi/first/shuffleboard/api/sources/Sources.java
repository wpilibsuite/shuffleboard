package edu.wpi.first.shuffleboard.api.sources;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Keeps track of all created data sources. This is primarily used for data recording and playback.
 */
public final class Sources {

  private static final Map<String, DataSource> sources = new HashMap<>();

  private Sources() {
  }

  /**
   * Registers the given data source.
   *
   * @param source the source to register
   */
  public static void register(DataSource<?> source) {
    if (sources.containsValue(source)) {
      // Already registered
      return;
    }
    sources.put(source.getId(), source);
  }

  /**
   * Unregisters a data source.
   *
   * @param source the source to unregister
   */
  public static void unregister(DataSource<?> source) {
    sources.remove(source.getId());
  }

  /**
   * Gets a list of all the known data sources
   *
   * @param type the source type to get the sources for
   */
  public static List<DataSource> forType(SourceType type) {
    return sources.values().stream()
        .filter(s -> s.getType().equals(type))
        .collect(Collectors.toList());
  }

  public static DataSource<?> forUri(String uri) {
    return computeIfAbsent(uri, () -> SourceTypes.forUri(uri));
  }

  @SuppressWarnings("unchecked") //NOPMD multiple occurrences of string literal
  public static <T> Optional<DataSource<T>> get(String id) {
    return Optional.ofNullable(sources.get(id));
  }

  @SuppressWarnings("unchecked") //NOPMD multiple occurrences of string literal
  public static <T> DataSource<T> computeIfAbsent(String uri, Supplier<DataSource<T>> sourceSupplier) {
    return sources.computeIfAbsent(uri, __ -> sourceSupplier.get());
  }

  public static void disconnectAll() {
    sources.forEach((__, source) -> source.disconnect());
  }

  public static void connectAll() {
    sources.forEach((__, source) -> source.connect());
  }

}
