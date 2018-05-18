package edu.wpi.first.shuffleboard.api.sources.recording;

import edu.wpi.first.shuffleboard.api.util.Registry;

import com.google.common.collect.ImmutableMap;

import java.util.Map;
import java.util.TreeMap;

public final class Converters extends Registry<Converter> {

  private final Map<String, Converter> converters = new TreeMap<>();

  private static final Converters defaultInstance = new Converters();

  public static Converters getDefault() {
    return defaultInstance;
  }

  @Override
  public void register(Converter item) {
    if (isRegistered(item)) {
      throw new IllegalArgumentException("Converter is already registered: " + item);
    }
    if (converters.containsKey(item.formatName())) {
      throw new IllegalArgumentException("A converter is already registered for format '" + item.formatName() + "'");
    }
    converters.put(item.formatName(), item);
    addItem(item);
  }

  @Override
  public void unregister(Converter item) {
    converters.remove(item.formatName());
    removeItem(item);
  }

  public Map<String, Converter> getConverters() {
    return ImmutableMap.copyOf(converters);
  }

}
