package edu.wpi.first.shuffleboard.app.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.widget.Widget;
import edu.wpi.first.shuffleboard.api.widget.Components;
import javafx.beans.property.Property;

@AnnotatedTypeAdapter(forType = Widget.class)
public class WidgetSaver implements ElementTypeAdapter<Widget> {

  @Override
  public JsonElement serialize(Widget src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("_type", src.getName());
    object.addProperty("_source", src.getSource().getId());
    for (Property p : src.getProperties()) {
      object.add(p.getName(), context.serialize(p.getValue()));
    }
    return object;
  }

  @Override
  public Widget deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    String type = obj.get("_type").getAsString();
    Widget widget = Components.getDefault().createWidget(type)
            .orElseThrow(() -> new JsonParseException("No widget found for " + type));

    String source = obj.get("_source").getAsString();
    widget.setSource(Sources.getDefault().forUri(source));

    for (Property p : widget.getProperties()) {
      p.setValue(context.deserialize(obj.get(p.getName()), p.getValue().getClass()));
    }

    return widget;
  }
}