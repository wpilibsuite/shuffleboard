package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class BooleanType implements SimpleDataType<Boolean> {

  @Override
  public Boolean getDefaultValue() {
    return false;
  }

  @Override
  public String getName() {
    return "Boolean";
  }

}
