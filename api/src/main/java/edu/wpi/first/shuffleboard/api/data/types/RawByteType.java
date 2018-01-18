package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public final class RawByteType extends SimpleDataType<byte[]> {

  public static final RawByteType Instance = new RawByteType();

  private RawByteType() {
    super("RawBytes", byte[].class);
  }

  @Override
  public byte[] getDefaultValue() {
    return new byte[0];
  }

}
