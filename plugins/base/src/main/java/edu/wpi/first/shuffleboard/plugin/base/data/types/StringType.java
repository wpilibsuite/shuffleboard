package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class StringType extends SimpleDataType<String> {

  public StringType() {
    super("String", String.class);
  }

  @Override
  public String getDefaultValue() {
    return "";
  }

}
