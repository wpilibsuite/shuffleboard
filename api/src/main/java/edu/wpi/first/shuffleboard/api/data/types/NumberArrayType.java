package edu.wpi.first.shuffleboard.api.data.types;

import edu.wpi.first.shuffleboard.api.data.SimpleDataType;

public final class NumberArrayType extends SimpleDataType<double[]> {

  public static final NumberArrayType Instance = new NumberArrayType();

  private NumberArrayType() {
    super("NumberArray", double[].class);
  }

  @Override
  public double[] getDefaultValue() {
    return new double[0];
  }

}
