package edu.wpi.first.shuffleboard.data.types;

import edu.wpi.first.shuffleboard.data.SimpleDataType;

public class UnknownType implements SimpleDataType<Object> {

  @Override
  public String getName() {
    return "Unknown";
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

}
