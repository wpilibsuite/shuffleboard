package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class StringArrayType extends SimpleDataType<String[]> {

  public StringArrayType() {
    super("StringArray", String[].class);
  }

  @Override
  public String[] getDefaultValue() {
    return new String[0];
  }

}
