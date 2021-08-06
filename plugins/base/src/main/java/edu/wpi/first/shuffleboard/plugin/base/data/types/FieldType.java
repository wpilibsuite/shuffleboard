package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.FieldData;

import java.util.Map;
import java.util.function.Function;

public class FieldType extends ComplexDataType<FieldData> {
  public static final FieldType Instance = new FieldType();

  private FieldType() {
    super("Field2d", FieldData.class);
  }

  @Override
  public Function<Map<String, Object>, FieldData> fromMap() {
    return FieldData::new;
  }

  @Override
  public FieldData getDefaultValue() {
    return new FieldData(new double[]{0, 0, 0}, Map.of());
  }
}
