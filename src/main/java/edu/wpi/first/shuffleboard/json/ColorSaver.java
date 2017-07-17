package edu.wpi.first.shuffleboard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import edu.wpi.first.shuffleboard.util.FxUtils;

import javafx.scene.paint.Color;

@AnnotatedTypeAdapter(forType = Color.class)
public class ColorSaver implements ElementTypeAdapter<Color> {

  @Override
  public JsonElement serialize(Color src, JsonSerializationContext context) {
    return context.serialize(FxUtils.toHexString(src));
  }

  @Override
  public Color deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    return Color.web(json.getAsString());
  }

}
