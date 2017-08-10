package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public class NumberArrayType extends SimpleDataType<double[]> {

  public NumberArrayType() {
    super("NumberArray", double[].class);
  }

  @Override
  public double[] getDefaultValue() {
    return new double[0];
  }

}
