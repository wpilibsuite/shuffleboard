package edu.wpi.first.shuffleboard.data.types;

import edu.wpi.first.shuffleboard.data.SimpleDataType;

public class StringType implements SimpleDataType<Object> {

  @Override
  public String getName() {
    return "String";
  }

  @Override
  public String getDefaultValue() {
    return "";
  }

}
