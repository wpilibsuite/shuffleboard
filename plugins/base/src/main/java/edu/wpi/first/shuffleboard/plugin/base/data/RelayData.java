package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.util.Maps;

import java.util.Map;

public class RelayData extends ComplexData<RelayData> {

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

  public RelayData(String name, State state) {
    this(name, state.getValue());
  }

  public RelayData(String name, String value) {
    this.name = name;
    this.value = value;
  }

  /**
   * Create a new RelayData object backed by the given map.
   *
   * @param map the map containing the data to be stored by this RelayData
   */
  public RelayData(Map<String, Object> map) {
    this((String) map.getOrDefault(".name", ""),
            (String) map.getOrDefault("Value", "Off"));
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.<String, Object>builder()
            .put(".name", name)
            .put("Value", value)
            .build();
  }

  public final String getName() {
    return name;
  }

  public final String getValue() {
    return value;
  }

  public final State getState() {
    return State.fromValue(value);
  }

  @Override
  public String toString() {
    return String.format("%s(name=%s, value=%s)", getClass().getSimpleName(), name, value);
  }

  public RelayData withState(State state) {
    return new RelayData(this.name, state);
  }

}
