package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.util.ReflectionUtils;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.property.Property;

@AnnotatedTypeAdapter(forType = Widget.class)
public class WidgetSaver implements ElementTypeAdapter<Widget> {

  private static final Logger log = Logger.getLogger(WidgetSaver.class.getName());

  private final SourcedRestorer sourcedRestorer = new SourcedRestorer();
  private final PropertySaver propertySaver = new PropertySaver();

  @Override
  public JsonElement serialize(Widget src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("_type", src.getName());
    for (int i = 0; i < src.getSources().size(); i++) {
      object.addProperty("_source" + i, src.getSources().get(i).getId());
    }
    object.addProperty("_title", src.getTitle());

    final List<Property<?>> savedProperties = PropertySaver.getPropertyFields(src.getClass())
        .map(f -> ReflectionUtils.<Property<?>>getUnchecked(src, f))
        .collect(Collectors.toList());

    // Save exported properties
    for (Property p : src.getProperties()) {
      if (!savedProperties.contains(p)) {
        PropertySaver.serializeProperty(context, object, p, p.getName());
      }
    }

    // Save @SaveThisProperty fields
    propertySaver.saveAnnotatedFields(src, context, object);

    // Save @SavePropertyFrom fields
    propertySaver.saveNestedProperties(src, context, object);

    return object;
  }

  @Override
  public Widget deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    String type = obj.get("_type").getAsString();
    Widget widget = Components.getDefault().createWidget(type)
        .orElseThrow(() -> new JsonParseException("No widget found for " + type));

    JsonElement title = obj.get("_title");
    if (title != null) {
      widget.setTitle(title.getAsString());
    }

    List<Property<?>> savedProperties = PropertySaver.getPropertyFields(widget.getClass())
        .map(f -> ReflectionUtils.<Property<?>>getUnchecked(widget, f))
        .collect(Collectors.toList());

    // Load exported properties
    for (Property p : widget.getProperties()) {
      if (savedProperties.contains(p)) {
        continue;
      }
      Object deserialized = context.deserialize(obj.get(p.getName()), p.getValue().getClass());
      if (deserialized != null) {
        p.setValue(deserialized);
      }
    }

    // Load @SaveThisProperty fields
    propertySaver.readAnnotatedFields(widget, context, obj);

    // Load @SavePropertyFrom fields
    propertySaver.readNestedProperties(widget, context, obj);

    // Load sources last - widgets can have saved properties that modify sources when they're added
    for (int i = 0; i > Integer.MIN_VALUE; i++) {
      String prop = "_source" + i;
      if (obj.has(prop)) {
        String uri = obj.get(prop).getAsString();
        Optional<? extends DataSource<?>> existingSource = Sources.getDefault().get(uri);
        try {
          if (existingSource.isPresent()) {
            widget.addSource(existingSource.get());
          } else {
            // Attempt to create a new source for the saved URI
            DataSource<?> dataSource = SourceTypes.getDefault().forUri(uri);
            // Check the source type to make sure it's the same as the one expected in the save file
            if (dataSource.getType().equals(SourceTypes.getDefault().typeForUri(uri))) {
              widget.addSource(dataSource);
            } else {
              log.warning("Saved source type is not present, adding destroyed source(s) instead");
              sourcedRestorer.addDestroyedSourcesForAllDataTypes(widget, uri);
            }
          }
        } catch (IncompatibleSourceException e) {
          log.log(Level.WARNING, "Couldn't load source, adding destroyed source(s) instead", e);
          sourcedRestorer.addDestroyedSourcesForAllDataTypes(widget, uri);
        }
      } else {
        break;
      }
    }

    return widget;
  }

}
