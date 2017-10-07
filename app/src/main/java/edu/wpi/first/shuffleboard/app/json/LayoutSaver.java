package edu.wpi.first.shuffleboard.app.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Sourced;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;

@AnnotatedTypeAdapter(forType = Layout.class)
public class LayoutSaver implements ElementTypeAdapter<Layout> {
  @Override
  public JsonElement serialize(Layout src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("_type", src.getName());

    if (src instanceof Sourced) {
      object.addProperty("_source", ((Sourced) src).getSource().getId());
    }

    Collection<Component> components = src.getChildren();
    JsonArray children = new JsonArray(components.size());
    components.forEach(c -> children.add(context.serialize(c, c.getClass())));
    object.add("_children", children);

    return object;
  }

  @Override
  public Layout deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    String name = json.getAsJsonObject().get("_type").getAsString();
    JsonArray children = json.getAsJsonObject().get("_children").getAsJsonArray();

    Layout layout = Components.getDefault().createComponent(name).flatMap(TypeUtils.optionalCast(Layout.class))
        .orElseThrow(() -> new JsonParseException("Can't find layout name " + name));

    if (layout instanceof Sourced) {
      String sourceUri = json.getAsJsonObject().get("_source").getAsString();
      DataSource<?> source = SourceTypes.getDefault().forUri(sourceUri);
      ((Sourced) layout).setSource(source);
    }

    children.forEach(child -> {
      String childName = child.getAsJsonObject().get("_type").getAsString();
      Optional<Type> childType = Components.getDefault().javaTypeFor(childName);
      childType.map(t -> context.deserialize(child, t))
          .flatMap(TypeUtils.optionalCast(Component.class))
          .ifPresent(layout::addChild);
    });

    return layout;
  }
}
