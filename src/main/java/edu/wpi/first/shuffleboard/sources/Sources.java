package edu.wpi.first.shuffleboard.sources;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class Sources {

  private static final Map<String, DataSource> sources = new HashMap<>();

  private Sources() {
  }

  public static void register(DataSource<?> source) {
    sources.put(source.getId(), source);
  }

  @SuppressWarnings("unchecked")
  public static <T> DataSource<T> forUri(String uri) {
    return sources.get(uri);
  }

  @SuppressWarnings("unchecked")
  public static <T> DataSource<T> getOrDefault(String uri, DataSource<T> defaultSource) {
    return sources.getOrDefault(uri, defaultSource);
  }

  @SuppressWarnings("unchecked")
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
