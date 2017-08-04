package edu.wpi.first.shuffleboard.data.types;

import edu.wpi.first.shuffleboard.data.ComplexDataType;
import edu.wpi.first.shuffleboard.data.SpeedControllerData;

import java.util.Map;
import java.util.function.Function;

public class SpeedControllerType implements ComplexDataType<SpeedControllerData> {

  @Override
  public Function<Map<String, Object>, SpeedControllerData> fromMap() {
    return SpeedControllerData::new;
  }

  @Override
  public String getName() {
    return "Speed Controller";
  }

  @Override
  public SpeedControllerData getDefaultValue() {
    return new SpeedControllerData("Example", 0.0);
  }

}
