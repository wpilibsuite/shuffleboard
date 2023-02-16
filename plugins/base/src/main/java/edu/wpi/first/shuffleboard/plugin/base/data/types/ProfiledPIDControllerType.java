package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.ProfiledPIDControllerData;

import java.util.Map;
import java.util.function.Function;

public final class ProfiledPIDControllerType extends ComplexDataType<ProfiledPIDControllerData> {

  public static final ProfiledPIDControllerType Instance = new ProfiledPIDControllerType();

  private ProfiledPIDControllerType() {
    super("ProfiledPIDController", ProfiledPIDControllerData.class);
  }

  @Override
  public Function<Map<String, Object>, ProfiledPIDControllerData> fromMap() {
    return ProfiledPIDControllerData::new;
  }

  @Override
  public ProfiledPIDControllerData getDefaultValue() {
    return new ProfiledPIDControllerData(0, 0, 0, 0);
  }

}
