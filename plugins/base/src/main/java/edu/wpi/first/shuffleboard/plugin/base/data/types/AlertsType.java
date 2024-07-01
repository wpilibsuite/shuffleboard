package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.api.util.Maps;
import edu.wpi.first.shuffleboard.plugin.base.data.AlertsData;

import java.util.Map;
import java.util.function.Function;

public final class AlertsType extends ComplexDataType<AlertsData> {

  /**
   * The name of data of this type as it would appear in a WPILib sendable's
   * {@code .type} entry; a differential drive base a {@code .type} of
   * "DifferentialDrive", a sendable chooser has it set to "String Chooser"; a
   * hypothetical 2D point would have it set to "Point2D".
   */
  private static final String TYPE_NAME = "Alerts";

  /**
   * The single instance of the point type. By convention, this is a
   * {@code public static final} field and the constructor is private to ensure
   * only a single instance of the data type exists.
   */
  public static final AlertsType Instance = new AlertsType();

  private AlertsType() {
    super(TYPE_NAME, AlertsData.class);
  }

  @Override
  public Function<Map<String, Object>, AlertsData> fromMap() {
    return map -> new AlertsData(
      Maps.getOrDefault(map, "errors", new String[0]),
      Maps.getOrDefault(map, "warnings", new String[0]),
      Maps.getOrDefault(map, "infos", new String[0])
    );
  }

  @Override
  public AlertsData getDefaultValue() {
    return new AlertsData(new String[] {}, new String[] {}, new String[] {});
  }
}
