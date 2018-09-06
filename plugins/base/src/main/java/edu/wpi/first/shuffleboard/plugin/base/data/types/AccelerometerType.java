package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.AccelerometerData;

import java.util.Map;
import java.util.function.Function;

public final class AccelerometerType extends ComplexDataType<AccelerometerData> {

  public static final AccelerometerType Instance = new AccelerometerType();

  private AccelerometerType() {
    super("Accelerometer", AccelerometerData.class);
  }

  @Override
  public Function<Map<String, Object>, AccelerometerData> fromMap() {
    return m -> new AccelerometerData((Double) m.getOrDefault("Value", 0.0));
  }

  @Override
  public AccelerometerData getDefaultValue() {
    return new AccelerometerData(0);
  }

}
