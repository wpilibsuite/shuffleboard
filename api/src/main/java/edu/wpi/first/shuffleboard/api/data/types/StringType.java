package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public final class StringType extends SimpleDataType<String> {

  public static final StringType Instance = new StringType();

  private StringType() {
    super("String", String.class);
  }

  @Override
  public String getDefaultValue() {
    return "";
  }

}
