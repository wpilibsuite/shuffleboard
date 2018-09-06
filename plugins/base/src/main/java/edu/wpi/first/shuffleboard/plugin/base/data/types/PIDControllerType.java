package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.PIDControllerData;

import java.util.Map;
import java.util.function.Function;

public final class PIDControllerType extends ComplexDataType<PIDControllerData> {

  public static final PIDControllerType Instance = new PIDControllerType();

  private PIDControllerType() {
    super("PIDController", PIDControllerData.class);
  }

  @Override
  public Function<Map<String, Object>, PIDControllerData> fromMap() {
    return PIDControllerData::new;
  }

  @Override
  public PIDControllerData getDefaultValue() {
    return new PIDControllerData(0, 0, 0, 0, 0, false);
  }

}
