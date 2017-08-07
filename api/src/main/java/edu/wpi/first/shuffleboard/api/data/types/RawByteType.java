package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class RawByteType implements SimpleDataType<byte[]> {

  @Override
  public String getName() {
    return "RawBytes";
  }

  @Override
  public byte[] getDefaultValue() {
    return new byte[0];
  }

}
