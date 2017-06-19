package edu.wpi.first.shuffleboard.data.types;

import edu.wpi.first.shuffleboard.data.SimpleDataType;

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
