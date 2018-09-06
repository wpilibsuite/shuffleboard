package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.json.AnnotatedTypeAdapter;
import edu.wpi.first.shuffleboard.api.json.ElementTypeAdapter;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.TileSize;
import edu.wpi.first.shuffleboard.app.components.Tile;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;

import javafx.scene.layout.GridPane;

@AnnotatedTypeAdapter(forType = WidgetPane.class)
public class WidgetPaneSaver implements ElementTypeAdapter<WidgetPane> {

  @Override
  public JsonElement serialize(WidgetPane src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("gridSize", src.getTileSize());
    object.addProperty("showGrid", src.isShowGrid());
    object.addProperty("hgap", src.getHgap());
    object.addProperty("vgap", src.getVgap());
    JsonObject tiles = new JsonObject();

    for (Tile<?> tile : src.getTiles()) {
      String x = GridPane.getColumnIndex(tile).toString();
      String y = GridPane.getRowIndex(tile).toString();
      String coordinate = String.join(",", x, y);

      JsonObject tileObject = new JsonObject();
      tileObject.add("size", context.serialize(tile.getSize(), TileSize.class));
      tileObject.add("content", context.serialize(tile.getContent(), tile.getContent().getClass()));

      tiles.add(coordinate, tileObject);
    }
    object.add("tiles", tiles);

    return object;
  }

  @Override
  public WidgetPane deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    JsonObject object = json.getAsJsonObject();
    WidgetPane pane = new WidgetPane();
    pane.setShowGrid(Optional.ofNullable(object.get("showGrid")).map(JsonElement::getAsBoolean).orElse(true));
    pane.setTileSize(object.get("gridSize").getAsDouble());
    pane.setHgap(object.get("hgap").getAsDouble());
    pane.setVgap(object.get("vgap").getAsDouble());

    JsonObject tiles = object.get("tiles").getAsJsonObject();

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
