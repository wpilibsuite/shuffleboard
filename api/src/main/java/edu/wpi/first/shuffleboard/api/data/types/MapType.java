package edu.wpi.first.shuffleboard.api.data.types;

import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.api.data.MapData;

import java.util.Map;
import java.util.function.Function;

public class MapType extends ComplexDataType<MapData> {

  public MapType() {
    super("Map", MapData.class);
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
