package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class BooleanArrayType extends SimpleDataType<boolean[]> {

  public BooleanArrayType() {
    super("BooleanArray", boolean[].class);
  }

  @Override
  public boolean[] getDefaultValue() {
    return new boolean[0];
  }

}
