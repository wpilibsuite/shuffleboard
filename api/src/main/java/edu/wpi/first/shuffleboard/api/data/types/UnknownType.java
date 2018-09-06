package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

/**
 * A catchall data type that is used when the type of data is unknown. This differs from {@link NoneType} in that this
 * class is for data that is present but whose type is indeterminate, while {@code NoneType} represents the type of
 * data that is not present at all.
 */
public final class UnknownType extends SimpleDataType<Object> {

  public static final UnknownType Instance = new UnknownType();

  private UnknownType() {
    super("Unknown", null);
  }

  @Override
  public Object getDefaultValue() {
    return null;
  }

}
