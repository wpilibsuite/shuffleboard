package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class StringType implements SimpleDataType<String> {

  @Override
  public String getName() {
    return "String";
  }

  @Override
  public String getDefaultValue() {
    return "";
  }

}
