package edu.wpi.first.shuffleboard.data.types;

import edu.wpi.first.shuffleboard.data.SimpleDataType;

public class NumberType implements SimpleDataType<Number> {

  @Override
  public String getName() {
    return "Number";
  }

  @Override
  public Double getDefaultValue() {
    return 0.0;
  }

}
