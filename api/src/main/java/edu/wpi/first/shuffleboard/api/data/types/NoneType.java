package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class NoneType extends SimpleDataType<Object> {

  public NoneType() {
    super("None", null);
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

}
