package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;

import java.util.Map;
import java.util.function.Function;

/**
 * Type of a simple subsystem. Subsystems don't contain any data in and of themselves.
 */
public final class SubsystemType extends ComplexDataType {

  public static final SubsystemType Instance = new SubsystemType();

  private SubsystemType() {
    super("LW Subsystem", Object.class);
  }

  @Override
  public Function<Map<String, Object>, ?> fromMap() {
    return __ -> null;
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

}
