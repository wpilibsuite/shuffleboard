package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class RawByteType extends SimpleDataType<byte[]> {

  public RawByteType() {
    super("RawBytes", byte[].class);
  }

  @Override
  public byte[] getDefaultValue() {
    return new byte[0];
  }

}
