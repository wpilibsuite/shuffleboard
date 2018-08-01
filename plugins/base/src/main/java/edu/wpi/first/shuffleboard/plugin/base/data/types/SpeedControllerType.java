package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.SpeedControllerData;

import java.util.Map;
import java.util.function.Function;

public final class SpeedControllerType extends ComplexDataType<SpeedControllerData> {

  public static final SpeedControllerType Instance = new SpeedControllerType();

  private SpeedControllerType() {
    super("Speed Controller", SpeedControllerData.class);
  }

  @Override
  public Function<Map<String, Object>, SpeedControllerData> fromMap() {
    return SpeedControllerData::new;
  }

  @Override
  public SpeedControllerData getDefaultValue() {
    return new SpeedControllerData("Example", 0.0, false);
  }

}
