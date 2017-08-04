package edu.wpi.first.shuffleboard.data;

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
