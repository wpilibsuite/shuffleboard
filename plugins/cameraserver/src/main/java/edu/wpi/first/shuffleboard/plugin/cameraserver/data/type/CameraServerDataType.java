package edu.wpi.first.shuffleboard.plugin.cameraserver.data.type;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;

import java.util.Map;
import java.util.function.Function;

public class CameraServerDataType extends ComplexDataType<CameraServerData> {

  public static final CameraServerDataType INSTANCE = new CameraServerDataType();

  public CameraServerDataType() {
    super("CameraServer", CameraServerData.class);
  }

  @Override
  public Function<Map<String, Object>, CameraServerData> fromMap() {
    return null;
  }

  @Override
  public CameraServerData getDefaultValue() {
    return new CameraServerData("Example", null);
  }

}
