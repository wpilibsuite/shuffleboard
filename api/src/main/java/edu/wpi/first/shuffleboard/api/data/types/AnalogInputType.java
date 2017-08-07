package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.AnalogInputData;
import edu.wpi.first.shuffleboard.api.data.ComplexDataType;

import java.util.Map;
import java.util.function.Function;

public class AnalogInputType implements ComplexDataType<AnalogInputData> {

  @Override
  public Function<Map<String, Object>, AnalogInputData> fromMap() {
    return AnalogInputData::new;
  }

  @Override
  public String getName() {
    return "Analog Input";
  }

  @Override
  public AnalogInputData getDefaultValue() {
    return new AnalogInputData("example", 0);
  }

}
