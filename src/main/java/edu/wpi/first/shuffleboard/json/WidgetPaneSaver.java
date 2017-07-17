package edu.wpi.first.shuffleboard.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import edu.wpi.first.shuffleboard.components.WidgetPane;
import edu.wpi.first.shuffleboard.components.WidgetTile;
import edu.wpi.first.shuffleboard.util.GridPoint;
import edu.wpi.first.shuffleboard.widget.TileSize;
import edu.wpi.first.shuffleboard.widget.Widget;
import javafx.scene.layout.GridPane;

import java.util.Map;

@AnnotatedTypeAdapter(forType = WidgetPane.class)
public class WidgetPaneSaver implements ElementTypeAdapter<WidgetPane> {

  @Override
  public JsonElement serialize(WidgetPane src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();

    for (WidgetTile tile : src.getTiles()) {
      String x = GridPane.getColumnIndex(tile).toString();
      String y = GridPane.getRowIndex(tile).toString();
      String coordinate = String.join(",", x, y);

      JsonObject tileObject = new JsonObject();
      tileObject.add("size", context.serialize(tile.getSize(), TileSize.class));
      tileObject.add("widget", context.serialize(tile.getWidget(), Widget.class));

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
      GridPoint coords = new GridPoint(Integer.valueOf(coordPart[0]), Integer.valueOf(coordPart[1]));

      JsonObject tile = tileLocation.getValue().getAsJsonObject();
      TileSize size = context.deserialize(tile.get("size"), TileSize.class);
      Widget widget = context.deserialize(tile.get("widget"), Widget.class);

      pane.addWidget(widget, coords, size);
    }

    return pane;
  }
}
