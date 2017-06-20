package edu.wpi.first.shuffleboard.data.types;

import edu.wpi.first.shuffleboard.data.SimpleDataType;

public class NumberArrayType implements SimpleDataType<double[]> {
  @Override
  public String getName() {
    return "NumberArray";
  }

  @Override
  public double[] getDefaultValue() {
    return new double[0];
  }

}
