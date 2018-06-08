package edu.wpi.first.shuffleboard.plugin.base.layout;

import edu.wpi.first.shuffleboard.api.json.AnnotatedTypeAdapter;
import edu.wpi.first.shuffleboard.api.json.ElementTypeAdapter;
import edu.wpi.first.shuffleboard.api.json.PropertySaver;
import edu.wpi.first.shuffleboard.api.util.GridPoint;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Layout;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import javafx.util.StringConverter;

@AnnotatedTypeAdapter(forType = GridLayout.class)
public final class GridLayoutSaver implements ElementTypeAdapter<GridLayout> {

  public static final GridLayoutSaver Instance = new GridLayoutSaver();

  private static final StringConverter<GridPoint> pointConverter = new StringConverter<GridPoint>() {

    private static final String SEPARATOR = ",";

    @Override
    public String toString(GridPoint point) {
      return point.col + SEPARATOR + point.row;
    }

    @Override
    public GridPoint fromString(String string) {
      String[] split = string.split(SEPARATOR);
      int col = Integer.parseInt(split[0]);
      int row = Integer.parseInt(split[1]);
      return new GridPoint(col, row);
    }
  };

  private final PropertySaver propertySaver = new PropertySaver();

  @Override
  public JsonElement serialize(GridLayout src, JsonSerializationContext context) {
    // Serialize properties etc. with LayoutSaver
    JsonElement initial = context.serialize(src, Layout.class);

    JsonObject obj = initial.getAsJsonObject();

    // Save children with their positions
    JsonObject children = new JsonObject();
    src.getContainers().forEach(container -> {
      GridPoint point = GridPoint.fromNode(container);
      children.add(pointConverter.toString(point), context.serialize(container.getChild()));
    });
    obj.add("_children", children);
    return obj;
  }

  @Override
  public GridLayout deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    GridLayout gridLayout = Components.getDefault().createComponent("Grid Layout")
        .flatMap(TypeUtils.optionalCast(GridLayout.class))
        .orElseThrow(() -> new JsonParseException("Can't create a new Grid Layout"));

    gridLayout.setTitle(obj.get("_title").getAsString());
    propertySaver.readAllProperties(gridLayout, context, obj);

    JsonObject children = obj.get("_children").getAsJsonObject();
    children.keySet().forEach(str -> {
      GridPoint point = pointConverter.fromString(str);
      String type = children.getAsJsonObject(str).get("_type").getAsString();
      Components.getDefault().javaTypeFor(type)
          .map(t -> context.deserialize(children.get(str), t))
          .flatMap(TypeUtils.optionalCast(Component.class))
          .ifPresent(component -> gridLayout.addChild(component, point));
    });

    return gridLayout;
  }
}
