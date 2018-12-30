package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.NamedData;
import edu.wpi.first.shuffleboard.api.util.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class SpeedControllerData extends NamedData<Double> {

  private final boolean controllable;

  public SpeedControllerData(String name, double value, boolean controllable) {
    super(name, value);
    this.controllable = controllable;
  }

  /**
   * Creates a new speed controller data object from a map.
   */
  public SpeedControllerData(Map<String, Object> map) {
    this(Maps.getOrDefault(map, "Name", ""),
        Maps.getOrDefault(map, "Value", 0.0),
        Maps.getOrDefault(map, ".controllable", false));
  }

  public boolean isControllable() {
    return controllable;
  }

  @Override
  public Map<String, Object> asMap() {
    Map<String, Object> map = new HashMap<>(super.asMap());
    map.put(".controllable", controllable);
    return map;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    SpeedControllerData that = (SpeedControllerData) obj;
    return super.equals(obj) && this.controllable == that.controllable;
  }

  @Override
  public int hashCode() {
    return super.hashCode() + 31 * super.hashCode() + Objects.hash(controllable);
  }

  @Override
  public String toHumanReadableString() {
    return "speed=" + getValue() + ", controllable=" + controllable;
  }
}
