package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.PowerDistributionData;

import java.util.Map;
import java.util.function.Function;

public final class PowerDistributionType extends ComplexDataType<PowerDistributionData> {

  public static final PowerDistributionType Instance = new PowerDistributionType();

  private PowerDistributionType() {
    super("PowerDistributionPanel", PowerDistributionData.class);
  }

  @Override
  public Function<Map<String, Object>, PowerDistributionData> fromMap() {
    return PowerDistributionData::new;
  }

  @Override
  public PowerDistributionData getDefaultValue() {
    return new PowerDistributionData(new double[16], 0.0, 0.0);
  }

}
