package edu.wpi.first.shuffleboard.sources;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public final class Sources {

  private static final Map<String, DataSource> sources = new HashMap<>();

  private Sources() {
  }

  public static void register(DataSource<?> source) {
    if (sources.containsValue(source)) {
      // Already registered
      return;
    }
    sources.put(source.getId(), source);
  }

  @SuppressWarnings("unchecked") //NOPMD multiple occurrences of string literal
  public static <T> DataSource<T> forUri(String uri) {
    return sources.get(uri);
  }

  @SuppressWarnings("unchecked") //NOPMD multiple occurrences of string literal
  public static <T> DataSource<T> getOrDefault(String uri, DataSource<T> defaultSource) {
    return sources.getOrDefault(uri, defaultSource);
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
