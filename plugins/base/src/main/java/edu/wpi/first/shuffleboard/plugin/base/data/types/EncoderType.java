package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.EncoderData;

import java.util.Map;
import java.util.function.Function;

public final class EncoderType extends ComplexDataType<EncoderData> {

  public static final EncoderType Instance = new EncoderType();

  private EncoderType() {
    super("Encoder", EncoderData.class);
  }

  @Override
  public Function<Map<String, Object>, EncoderData> fromMap() {
    return EncoderData::new;
  }

  @Override
  public EncoderData getDefaultValue() {
    return new EncoderData("Example", 0, 0, 0);
  }

}
