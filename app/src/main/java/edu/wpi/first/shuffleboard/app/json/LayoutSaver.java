package edu.wpi.first.shuffleboard.app.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.app.widget.Layout;

import java.util.Collection;

@AnnotatedTypeAdapter(forType = Layout.class)
public class LayoutSaver implements ElementTypeAdapter<Layout> {
  @Override
  public JsonElement serialize(Layout src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("_type", src.getName());

    Collection<Component> components = src.getChildren();
    JsonArray children = new JsonArray(components.size());
    components.forEach(c -> children.add(context.serialize(c, c.getClass())));
    object.add("_children", children);

    return object;
  }

  @Override
  public Layout deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    return null;
  }
}
