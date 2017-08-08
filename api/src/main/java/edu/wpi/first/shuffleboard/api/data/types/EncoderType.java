package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.api.data.EncoderData;

import java.util.Map;
import java.util.function.Function;

public class EncoderType implements ComplexDataType<EncoderData> {

  @Override
  public Function<Map<String, Object>, EncoderData> fromMap() {
    return EncoderData::new;
  }

  @Override
  public String getName() {
    return "Encoder";
  }

  @Override
  public EncoderData getDefaultValue() {
    return new EncoderData("Example", 0, 0, 0);
  }

}
