package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.RobotPreferencesData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public final class RobotPreferencesType extends ComplexDataType<RobotPreferencesData> {

  public static final RobotPreferencesType Instance = new RobotPreferencesType();

  private RobotPreferencesType() {
    super("RobotPreferences", RobotPreferencesData.class);
  }

  @Override
  public Function<Map<String, Object>, RobotPreferencesData> fromMap() {
    return RobotPreferencesData::new;
  }

  @Override
  public RobotPreferencesData getDefaultValue() {
    return new RobotPreferencesData(new HashMap<>());
  }

}
