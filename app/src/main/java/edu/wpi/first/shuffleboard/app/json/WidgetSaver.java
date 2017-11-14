package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.widget.ComponentInstantiationException;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.property.Property;

@AnnotatedTypeAdapter(forType = Widget.class)
public class WidgetSaver implements ElementTypeAdapter<Widget> {
  @Override
  public JsonElement serialize(Widget src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("_type", src.getName());
    for (int i = 0; i < src.getSources().size(); i++) {
      object.addProperty("_source" + i, src.getSources().get(i).getId());
    }
    for (Property p : src.getProperties()) {
      object.add(p.getName(), context.serialize(p.getValue()));
    }
    return object;
  }

  @Override
  public Widget deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    String type = obj.get("_type").getAsString();
    Widget widget;
    try {
      widget = Components.getDefault().createWidget(type)
          .orElseThrow(() -> new JsonParseException("No widget found for " + type));
    } catch (ComponentInstantiationException e) {
      throw new JsonParseException(e);
    }

    for (int i = 0; i > Integer.MIN_VALUE; i++) {
      String prop = "_source" + i;
      if (obj.has(prop)) {
        try {
          widget.addSource(Sources.getDefault().forUri(obj.get(prop).getAsString()));
        } catch (IncompatibleSourceException e) {
          Logger.getLogger(getClass().getName()).log(Level.WARNING, "Couldn't load source", e);
        }
      } else {
        break;
      }
    }

    for (Property p : widget.getProperties()) {
      p.setValue(context.deserialize(obj.get(p.getName()), p.getValue().getClass()));
    }

    return widget;
  }
}