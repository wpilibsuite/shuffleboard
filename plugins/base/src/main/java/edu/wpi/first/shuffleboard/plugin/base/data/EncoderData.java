package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.util.Maps;

import java.util.Map;

public class EncoderData extends ComplexData<EncoderData> {

  private final String name;
  private final double distancePerTick;
  private final double distance;
  private final double speed;

  @SuppressWarnings("JavadocMethod")
  public EncoderData(String name, double distancePerTick, double distance, double speed) {
    this.name = name;
    this.distancePerTick = distancePerTick;
    this.distance = distance;
    this.speed = speed;
  }

  @SuppressWarnings("JavadocMethod")
  public EncoderData(Map<String, Object> map) {
    name = (String) map.getOrDefault("Name", "");
    distancePerTick = (double) map.getOrDefault("Distance per Tick", 0.0);
    distance = (double) map.getOrDefault("Distance", 0.0);
    speed = (double) map.getOrDefault("Speed", 0.0);
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.<String, Object>builder()
        .put("Name", name)
        .put("Distance per Tick", distancePerTick)
        .put("Distance", distance)
        .put("Speed", speed)
        .build();
  }

  public String getName() {
    return name;
  }

  public double getDistancePerTick() {
    return distancePerTick;
  }

  public double getDistance() {
    return distance;
  }

  public double getSpeed() {
    return speed;
  }

  @Override
  public String toHumanReadableString() {
    return "Distance=" + distance + ", speed=" + speed;
  }
}
