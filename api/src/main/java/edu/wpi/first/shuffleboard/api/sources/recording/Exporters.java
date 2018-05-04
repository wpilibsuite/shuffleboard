package edu.wpi.first.shuffleboard.api.sources.recording;

import edu.wpi.first.shuffleboard.api.util.Registry;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.TreeMap;

public final class Exporters extends Registry<Exporter> {

  private final Map<String, Exporter> exporters = new TreeMap<>();

  private static final Exporters defaultInstance = new Exporters();

  public static Exporters getDefault() {
    return defaultInstance;
  }

  @Override
  public void register(Exporter item) {
    if (isRegistered(item)) {
      throw new IllegalArgumentException("Item is already registered: " + item);
    }
    exporters.put(item.formatName(), item);
    addItem(item);
  }

  @Override
  public void unregister(Exporter item) {
    exporters.remove(item.formatName());
    removeItem(item);
  }

  public Map<String, Exporter> getExporters() {
    return ImmutableMap.copyOf(exporters);
  }

}
