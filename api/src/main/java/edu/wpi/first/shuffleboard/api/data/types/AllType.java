package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

/**
 * A catchall data type that represents all data. This should only be used by widgets that can truly show <i>any</i>
 * data.
 */
public class AllType extends SimpleDataType<Object> {

  public AllType() {
    super("All", Object.class);
  }

  @Override
  public Object getDefaultValue() {
    return new Object();
  }

}
