package edu.wpi.first.shuffleboard.data.types;

import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.data.ComplexDataType;
import edu.wpi.first.shuffleboard.data.MapData;

import java.util.Map;
import java.util.function.Function;

public class MapType implements ComplexDataType<MapData> {

  @Override
  public String getName() {
    return "Map";
  }

  @Override
  public MapData getDefaultValue() {
    return new MapData(ImmutableMap.of());
  }

  @Override
  public Function<Map<String, Object>, MapData> fromMap() {
    return MapData::new;
  }

}
