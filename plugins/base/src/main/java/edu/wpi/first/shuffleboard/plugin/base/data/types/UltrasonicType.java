package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.api.util.Maps;
import edu.wpi.first.shuffleboard.plugin.base.data.UltrasonicData;

import java.util.Map;
import java.util.function.Function;

public final class UltrasonicType extends ComplexDataType<UltrasonicData> {

  public static final UltrasonicType Instance = new UltrasonicType();

  private UltrasonicType() {
    super("Ultrasonic", UltrasonicData.class);
  }

  @Override
  public Function<Map<String, Object>, UltrasonicData> fromMap() {
    return map -> new UltrasonicData(
        Maps.getOrDefault(map, "Value", 0.0)
    );
  }

  @Override
  public UltrasonicData getDefaultValue() {
    return new UltrasonicData(0);
  }
}
