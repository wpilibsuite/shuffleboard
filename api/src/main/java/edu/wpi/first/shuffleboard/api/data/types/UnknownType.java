package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class UnknownType extends SimpleDataType<Object> {

  public UnknownType() {
    super("Unknown", null);
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

}
