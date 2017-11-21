package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class BooleanType extends SimpleDataType<Boolean> {

  public BooleanType() {
    super("Boolean", Boolean.class);
  }

  @Override
  public Boolean getDefaultValue() {
    return false;
  }

}
