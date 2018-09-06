package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.PIDCommandData;

import java.util.Map;
import java.util.function.Function;

public final class PIDCommandType extends ComplexDataType<PIDCommandData> {

  public static final PIDCommandType Instance = new PIDCommandType();

  private PIDCommandType() {
    super("PIDCommand", PIDCommandData.class);
  }

  @Override
  public Function<Map<String, Object>, PIDCommandData> fromMap() {
    return PIDCommandData::new;
  }

  @Override
  public PIDCommandData getDefaultValue() {
    return new PIDCommandData("", false, false, 0, 0, 0, 0, 0, false);
  }

}
