package edu.wpi.first.shuffleboard.api.data;

import edu.wpi.first.shuffleboard.api.util.Maps;

import java.util.Map;

/**
 * Data that only contains a name and value.
 *
 * @param <T> the type of the value
 */
public abstract class NamedData<T> extends ComplexData<NamedData<T>> {

  private final String name;
  private final T value;

  protected NamedData(String name, T value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.<String, Object>builder()
        .put("Name", name)
        .put("Value", value)
        .build();
  }

  public final String getName() {
    return name;
  }

  public final T getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.format("%s(name=%s, value=%s)", getClass().getSimpleName(), name, value);
  }
}
