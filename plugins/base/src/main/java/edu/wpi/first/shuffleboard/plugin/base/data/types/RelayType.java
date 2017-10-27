package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.RelayData;

import java.util.Map;
import java.util.function.Function;

public class RelayType extends ComplexDataType<RelayData> {

  public RelayType() {
    super("Relay", RelayData.class);
  }

  @Override
  public Function<Map<String, Object>, RelayData> fromMap() {
    return RelayData::new;
  }

  @Override
  public RelayData getDefaultValue() {
    return new RelayData("Example Relay", "Off");
  }

}
