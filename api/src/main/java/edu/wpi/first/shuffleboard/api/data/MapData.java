package edu.wpi.first.shuffleboard.api.data;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

public class MapData extends ComplexData<MapData> {

  private final ImmutableMap<String, Object> map;

  public MapData(Map<String, Object> map) {
    this.map = ImmutableMap.copyOf(map);
  }

  @Override
  public ImmutableMap<String, Object> asMap() {
    return map;
  }

  public Object get(String key) {
    return map.get(key);
  }

  public boolean isEmpty() {
    return map.isEmpty();
  }

  public boolean containsKey(String key) {
    return map.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  /**
   * Creates a new MapData instance that is identical to this one, but with a new value for the given key.
   *
   * @param key   the key to set
   * @param value the new value to put
   *
   * @return a new MapData instance containing the change
   */
  public MapData put(String key, Object value) {
    HashMap<String, Object> map = new HashMap<>(this.map);
    map.put(key, value);
    return new MapData(map);
  }

  @Override
  public String toString() {
    return map.toString();
  }

}
