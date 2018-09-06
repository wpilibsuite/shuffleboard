package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.json.AnnotatedTypeAdapter;
import edu.wpi.first.shuffleboard.api.json.ElementTypeAdapter;
import edu.wpi.first.shuffleboard.app.components.DashboardTab;
import edu.wpi.first.shuffleboard.app.components.DashboardTabPane;
import edu.wpi.first.shuffleboard.app.components.ProcedurallyDefinedTab;
import edu.wpi.first.shuffleboard.app.components.WidgetPane;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.control.Tab;

@AnnotatedTypeAdapter(forType = DashboardTabPane.class)
public class DashboardTabPaneSaver implements ElementTypeAdapter<DashboardTabPane> {

  @Override
  public JsonElement serialize(DashboardTabPane src, JsonSerializationContext context) {
    JsonArray tabs = new JsonArray();

    for (Tab t : src.getTabs()) {
      if (t instanceof DashboardTab && !(t instanceof ProcedurallyDefinedTab)) {
        DashboardTab tab = (DashboardTab) t;

        JsonObject object = new JsonObject();
        object.addProperty("title", tab.getTitle());
        object.addProperty("autoPopulate", tab.isAutoPopulate());
        object.addProperty("autoPopulatePrefix", tab.getSourcePrefix());
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

    for (JsonElement i : jsonTabs) {
      JsonObject obj = i.getAsJsonObject();
      String title = obj.get("title").getAsString();
      DashboardTab tab = new DashboardTab(title);
      tab.setWidgetPane(context.deserialize(obj.get("widgetPane"), WidgetPane.class));
      tab.setSourcePrefix(obj.get("autoPopulatePrefix").getAsString());
      tab.setAutoPopulate(obj.get("autoPopulate").getAsBoolean());
      tabs.add(tab);
    }

    return new DashboardTabPane(tabs.toArray(new Tab[]{}));
  }
}
