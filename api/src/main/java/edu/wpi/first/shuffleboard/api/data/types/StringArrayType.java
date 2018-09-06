package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public final class StringArrayType extends SimpleDataType<String[]> {

  public static final StringArrayType Instance = new StringArrayType();

  private StringArrayType() {
    super("StringArray", String[].class);
  }

  @Override
  public String[] getDefaultValue() {
    return new String[0];
  }

}
