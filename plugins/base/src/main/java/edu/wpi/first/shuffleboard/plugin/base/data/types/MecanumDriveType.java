package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.MecanumDriveData;

import java.util.Map;
import java.util.function.Function;

public final class MecanumDriveType extends ComplexDataType<MecanumDriveData> {

  public static final MecanumDriveType Instance = new MecanumDriveType();

  private MecanumDriveType() {
    super("MecanumDrive", MecanumDriveData.class);
  }

  @Override
  public Function<Map<String, Object>, MecanumDriveData> fromMap() {
    return MecanumDriveData::fromMap;
  }

  @Override
  public MecanumDriveData getDefaultValue() {
    return new MecanumDriveData(0, 0, 0, 0, false);
  }

}
