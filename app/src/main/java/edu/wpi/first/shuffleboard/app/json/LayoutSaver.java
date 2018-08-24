package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.json.AnnotatedTypeAdapter;
import edu.wpi.first.shuffleboard.api.json.ElementTypeAdapter;
import edu.wpi.first.shuffleboard.api.json.PropertySaver;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Layout;
import edu.wpi.first.shuffleboard.api.widget.Sourced;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;

@AnnotatedTypeAdapter(forType = Layout.class)
public class LayoutSaver implements ElementTypeAdapter<Layout> {

  private final SourcedRestorer sourcedRestorer = new SourcedRestorer();
  private final PropertySaver propertySaver = new PropertySaver();

  @Override
  public JsonElement serialize(Layout src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("_type", src.getName());

    if (src instanceof Sourced) {
      ObservableList<DataSource> sources = ((Sourced) src).getSources();
      for (int i = 0; i < sources.size(); i++) {
        object.addProperty("_source" + i, sources.get(i).getId());
      }
    }

    object.addProperty("_title", src.getTitle());

    propertySaver.saveAllProperties(src, context, object);

    Collection<Component> components = src.getChildren();
    JsonArray children = new JsonArray(components.size());
    components.forEach(c -> children.add(context.serialize(c, c.getClass())));
    object.add("_children", children);

    return object;
  }

  @Override
  public Layout deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    String name = obj.get("_type").getAsString();

    Layout layout = Components.getDefault().createComponent(name).flatMap(TypeUtils.optionalCast(Layout.class))
        .orElseThrow(() -> new JsonParseException("Can't find layout name " + name));

    propertySaver.readAllProperties(layout, context, obj);

    if (layout instanceof Sourced) {
      Sourced sourcedLayout = (Sourced) layout;
      for (int i = 0; i > Integer.MIN_VALUE; i++) {
        String prop = "_source" + i;
        if (obj.has(prop)) {
          String uri = obj.get(prop).getAsString();
          Optional<? extends DataSource<?>> source = Sources.getDefault().get(uri);
          try {
            if (source.isPresent()) {
              sourcedLayout.addSource(source.get());
            } else {
              sourcedRestorer.addDestroyedSourcesForAllDataTypes(sourcedLayout, uri);
            }
          } catch (IncompatibleSourceException e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Couldn't load source", e);
            sourcedRestorer.addDestroyedSourcesForAllDataTypes(sourcedLayout, uri);
          }
        } else {
          break;
        }
      }
    }

    JsonElement title = obj.get("_title");
    if (title != null) {
      layout.setTitle(title.getAsString());
    }

    JsonArray children = obj.get("_children").getAsJsonArray();
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
