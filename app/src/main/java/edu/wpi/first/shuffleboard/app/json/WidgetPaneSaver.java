package edu.wpi.first.shuffleboard.app.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;

import java.lang.reflect.Type;
import java.util.Map;

import javafx.scene.layout.GridPane;

@AnnotatedTypeAdapter(forType = WidgetPane.class)
public class WidgetPaneSaver implements ElementTypeAdapter<WidgetPane> {

  @Override
  public JsonElement serialize(WidgetPane src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();

    for (Tile<?> tile : src.getTiles()) {
      String x = GridPane.getColumnIndex(tile).toString();
      String y = GridPane.getRowIndex(tile).toString();
      String coordinate = String.join(",", x, y);

      JsonObject tileObject = new JsonObject();
      tileObject.add("size", context.serialize(tile.getSize(), TileSize.class));
      tileObject.add("content", context.serialize(tile.getContent(), tile.getContent().getClass()));

      object.add(coordinate, tileObject);
    }

    return object;
  }

  @Override
  public WidgetPane deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    JsonObject tiles = json.getAsJsonObject();
    WidgetPane pane = new WidgetPane();

    for (Map.Entry<String, JsonElement> tileLocation : tiles.entrySet()) {
      String[] coordPart = tileLocation.getKey().split(",");
      GridPoint coords = new GridPoint(Integer.parseInt(coordPart[0]), Integer.parseInt(coordPart[1]));

      JsonObject tile = tileLocation.getValue().getAsJsonObject();
      TileSize size = context.deserialize(tile.get("size"), TileSize.class);

      String childName = tile.get("content").getAsJsonObject().get("_type").getAsString();
      Type childType = Components.getDefault().javaTypeFor(childName)
          .orElseThrow(() -> new JsonParseException("Can't find component name " + childName));

      Component component = context.deserialize(tile.get("content"), childType);
      pane.addComponent(component, coords, size);
    }

    return pane;
  }
}
