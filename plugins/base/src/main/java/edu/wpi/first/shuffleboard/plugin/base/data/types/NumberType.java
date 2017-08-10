package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class NumberType extends SimpleDataType<Number> {

  public NumberType() {
    super("Number", Number.class);
  }

  @Override
  public Double getDefaultValue() {
    return 0.0;
  }

}
