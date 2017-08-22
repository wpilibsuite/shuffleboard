package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.NamedData;

import java.util.Map;

public class SpeedControllerData extends NamedData<Double> {

  public SpeedControllerData(String name, double value) {
    super(name, value);
  }

  public SpeedControllerData(Map<String, Object> map) {
    this((String) map.getOrDefault("Name", ""),
        (Double) map.getOrDefault("Value", 0.0));
  }

}
