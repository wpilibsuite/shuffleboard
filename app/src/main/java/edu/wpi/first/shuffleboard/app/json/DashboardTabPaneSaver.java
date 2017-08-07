package edu.wpi.first.shuffleboard.app.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;
import javafx.scene.control.Tab;

import java.util.ArrayList;
import java.util.List;

@AnnotatedTypeAdapter(forType = DashboardTabPane.class)
public class DashboardTabPaneSaver implements ElementTypeAdapter<DashboardTabPane> {
  @Override
  public JsonElement serialize(DashboardTabPane src, JsonSerializationContext context) {
    JsonArray tabs = new JsonArray();

    for (Tab t : src.getTabs()) {
      if (t instanceof DashboardTabPane.DashboardTab) {
        DashboardTabPane.DashboardTab tab = (DashboardTabPane.DashboardTab) t;

        JsonObject object = new JsonObject();
        object.addProperty("title", tab.getTitle());
        object.add("widgetPane", context.serialize(tab.getWidgetPane()));

        tabs.add(object);
      }
    }

    return tabs;
  }

  @Override
  public DashboardTabPane deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    JsonArray jsonTabs = json.getAsJsonArray();
    List<Tab> tabs = new ArrayList<>(jsonTabs.size());

    for (JsonElement i : json.getAsJsonArray()) {
      String title = i.getAsJsonObject().get("title").getAsString();
      DashboardTabPane.DashboardTab tab = new DashboardTabPane.DashboardTab(title);
      tab.setWidgetPane(context.deserialize(i.getAsJsonObject().get("widgetPane"), WidgetPane.class));
      tabs.add(tab);
    }

    return new DashboardTabPane(tabs.toArray(new Tab[]{}));
  }
}
