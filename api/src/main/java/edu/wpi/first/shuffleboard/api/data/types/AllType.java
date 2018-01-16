package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

/**
 * A catchall data type that represents all data. This should only be used by widgets that can truly show <i>any</i>
 * data.
 */
public final class AllType extends SimpleDataType<Object> {

  public static final AllType Instance = new AllType();
  private final Object defaultValue = new Object();

  private AllType() {
    super("All", Object.class);
  }

  @Override
  public Object getDefaultValue() {
    return defaultValue;
  }

}
