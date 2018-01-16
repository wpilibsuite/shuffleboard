package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.plugin.base.data.AnalogInputData;
import edu.wpi.first.shuffleboard.api.data.ComplexDataType;

import java.util.Map;
import java.util.function.Function;

public final class AnalogInputType extends ComplexDataType<AnalogInputData> {

  public static final AnalogInputType Instance = new AnalogInputType();

  private AnalogInputType() {
    super("Analog Input", AnalogInputData.class);
  }

  @Override
  public Function<Map<String, Object>, AnalogInputData> fromMap() {
    return AnalogInputData::new;
  }

  @Override
  public AnalogInputData getDefaultValue() {
    return new AnalogInputData("example", 0);
  }

}
