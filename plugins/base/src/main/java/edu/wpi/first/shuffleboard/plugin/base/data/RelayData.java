package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.util.Maps;

import java.util.Map;

public final class RelayData extends ComplexData<RelayData> {

  public enum State {
    OFF("Off"),
    ON("On"),
    FORWARD("Forward"),
    REVERSE("Reverse");

    private final String value;

    State(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }

    /**
     * Get the State corresponding to the given value.
     *
     * @param str the value whose corresponding State should be returned
     *
     * @return the State corresponding to the given value, or null if there is no such State
     */
    public static State fromValue(String str) {
      for (State state : State.values()) {
        if (state.value.equals(str)) {
          return state;
        }
      }
      return null;
    }
  }

  private final String name;
  private final String value;
  private final boolean controllable;

  public RelayData(String name, State state, boolean controllable) {
    this(name, state.getValue(), controllable);
  }

  /**
   * Creates a new relay data object.
   *
   * @param name         the name of the relay
   * @param value        the value of the relay ("Off", "On", "Forward", or "Reverse")
   * @param controllable if this relay is user-controllable
   */
  public RelayData(String name, String value, boolean controllable) {
    this.name = name;
    this.value = value;
    this.controllable = controllable;
  }

  /**
   * Create a new RelayData object backed by the given map.
   *
   * @param map the map containing the data to be stored by this RelayData
   */
  public RelayData(Map<String, Object> map) {
    this(Maps.getOrDefault(map, ".name", ""),
        Maps.getOrDefault(map, "Value", "Off"),
        Maps.getOrDefault(map, ".controllable", false));
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.<String, Object>builder()
        .put(".name", name)
        .put("Value", value)
        .put(".controllable", controllable)
        .build();
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public State getState() {
    return State.fromValue(value);
  }

  public boolean isControllable() {
    return controllable;
  }

  @Override
  public String toString() {
    return String.format("RelayData(name=%s, value=%s, controllable=%s)", name, value, controllable);
  }

  @Override
  public String toHumanReadableString() {
    return value;
  }

  public RelayData withState(State state) {
    return new RelayData(this.name, state, controllable);
  }

}
