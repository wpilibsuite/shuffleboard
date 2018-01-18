package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public final class BooleanType extends SimpleDataType<Boolean> {

  public static final BooleanType Instance = new BooleanType();

  private BooleanType() {
    super("Boolean", Boolean.class);
  }

  @Override
  public Boolean getDefaultValue() {
    return false;
  }

}
