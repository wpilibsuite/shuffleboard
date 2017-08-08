package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class NoneType implements SimpleDataType {

  @Override
  public String getName() {
    return "None";
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

}
