package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.RelayData;

import java.util.Map;
import java.util.function.Function;

public final class RelayType extends ComplexDataType<RelayData> {

  public static final RelayType Instance = new RelayType();

  private RelayType() {
    super("Relay", RelayData.class);
  }

  @Override
  public Function<Map<String, Object>, RelayData> fromMap() {
    return RelayData::new;
  }

  @Override
  public RelayData getDefaultValue() {
    return new RelayData("Example Relay", "Off", false);
  }

}
