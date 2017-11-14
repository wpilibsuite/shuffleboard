package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

/**
 * A catchall data type that represents the type of "null" or "nonpresent" data.
 */
public class NoneType extends SimpleDataType {

  public NoneType() {
    super("None", null);
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

}
