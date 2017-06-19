package edu.wpi.first.shuffleboard.data.types;

import edu.wpi.first.shuffleboard.data.SimpleDataType;

public class BooleanArrayType implements SimpleDataType<boolean[]> {

  @Override
  public String getName() {
    return "BooleanArray";
  }

  @Override
  public boolean[] getDefaultValue() {
    return new boolean[0];
  }

}
