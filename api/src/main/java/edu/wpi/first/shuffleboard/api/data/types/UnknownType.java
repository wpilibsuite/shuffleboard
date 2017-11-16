package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

/**
 * Represents an "unknown" data type; that is, data is present, but the type could not be determined.
 */
public class UnknownType extends SimpleDataType<Object> {

  public UnknownType() {
    super("Unknown", null);
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

}
