package edu.wpi.first.shuffleboard.data.types;

import edu.wpi.first.shuffleboard.data.SimpleDataType;

public class AllType implements SimpleDataType<Object> {

  @Override
  public String getName() {
    return "All";
  }

  @Override
  public Object getDefaultValue() {
    return new Object();
  }

}
