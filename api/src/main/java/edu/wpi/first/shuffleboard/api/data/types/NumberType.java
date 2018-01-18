package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public final class NumberType extends SimpleDataType<Number> {

  public static final NumberType Instance = new NumberType();

  private NumberType() {
    super("Number", Number.class);
  }

  @Override
  public Double getDefaultValue() {
    return 0.0;
  }

}
