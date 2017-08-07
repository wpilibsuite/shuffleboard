package edu.wpi.first.shuffleboard.api.data;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public abstract class NamedData<T> extends ComplexData<NamedData<T>> {

  private final String name;
  private final T value;

  protected NamedData(String name, T value) {
    this.name = name;
    this.value = value;
  }

  @Override
  public Map<String, Object> asMap() {
    return ImmutableMap.of("Name", name, "Value", value);
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
