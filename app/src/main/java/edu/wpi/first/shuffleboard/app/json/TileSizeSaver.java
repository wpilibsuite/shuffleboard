package edu.wpi.first.shuffleboard.app.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import edu.wpi.first.shuffleboard.api.json.AnnotatedTypeAdapter;
import edu.wpi.first.shuffleboard.api.json.ElementTypeAdapter;
import edu.wpi.first.shuffleboard.api.widget.TileSize;

@AnnotatedTypeAdapter(forType = TileSize.class)
public class TileSizeSaver implements ElementTypeAdapter<TileSize> {
  @Override
  public JsonElement serialize(TileSize src, JsonSerializationContext context) {
    JsonArray array = new JsonArray(2);
    array.add(src.getWidth());
    array.add(src.getHeight());
    return array;
  }

  @Override
  public TileSize deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    int width = json.getAsJsonArray().get(0).getAsInt();
    int height = json.getAsJsonArray().get(1).getAsInt();
    try {
      return new TileSize(width, height);
    } catch (IllegalArgumentException e) {
      throw new JsonParseException("Illegal TileSize. Expected positive integers, found " + json.getAsString(), e);
    }
  }
}
