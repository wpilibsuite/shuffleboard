package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public final class BooleanArrayType extends SimpleDataType<boolean[]> {

  public static final BooleanArrayType Instance = new BooleanArrayType();

  private BooleanArrayType() {
    super("BooleanArray", boolean[].class);
  }

  @Override
  public boolean[] getDefaultValue() {
    return new boolean[0];
  }

}
