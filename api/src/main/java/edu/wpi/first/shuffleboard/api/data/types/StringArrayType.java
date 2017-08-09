package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class StringArrayType implements SimpleDataType<String[]> {

  @Override
  public String getName() {
    return "StringArray";
  }

  @Override
  public String[] getDefaultValue() {
    return new String[0];
  }

}
