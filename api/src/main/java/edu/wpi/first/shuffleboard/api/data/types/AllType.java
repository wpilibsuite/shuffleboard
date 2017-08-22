package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class AllType extends SimpleDataType<Object> {

  public AllType() {
    super("All", Object.class);
  }

  @Override
  public Object getDefaultValue() {
    return new Object();
  }

}
