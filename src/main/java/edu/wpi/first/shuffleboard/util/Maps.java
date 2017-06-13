package edu.wpi.first.shuffleboard.util;

import java.util.HashMap;
import java.util.Map;

public final class Maps {

  private Maps() {
  }

  public static <K, V> Builder<K, V> builder() {
    return new Builder<>();
  }

  public static final class Builder<K, V> {

    private final Map<K, V> built = new HashMap<>();

    public Builder<K, V> put(K key, V value) {
      built.put(key, value);
      return this;
    }

    public Map<K, V> build() {
      return built;
    }

  }

}
