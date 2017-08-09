package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

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
