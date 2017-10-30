package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.WidgetPropertySheet;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.RobotPreferencesData;

import org.controlsfx.control.PropertySheet;

import java.util.Comparator;
import java.util.Map;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;

/**
 * A widget for displaying and editing preferences for an FRC robot.
 */
@Description(name = "Robot Preferences", dataTypes = RobotPreferencesData.class)
@ParametrizedController("RobotPreferencesWidget.fxml")
public class RobotPreferencesWidget extends SimpleAnnotatedWidget<RobotPreferencesData> {

  @FXML
  private Pane root;
  @FXML
  private PropertySheet propertySheet;

  // Keep map of properties
  // If we just used the data map in the property sheet, a new item (and editor) would be created for each change
  // For example, typing a new character in a text field would remove that field and replace it with a new one
  // This approach makes it so that editors don't appear to lose focus when users are interacting with them
  private final ObservableMap<String, ObjectProperty<Object>> wrapperProperties = FXCollections.observableHashMap();

  private static final Comparator<PropertySheet.Item> itemSorter =
      Comparator.comparing(PropertySheet.Item::getName, AlphanumComparator.INSTANCE);

  @FXML
  private void initialize() {
    dataOrDefault.addListener((__, prevData, curData) -> {
      Map<String, Object> updated = curData.changesFrom(prevData);
      if (prevData != null) {
        // Remove items for any deleted robot preferences
        prevData.asMap().entrySet().stream()
            .map(Map.Entry::getKey)
            .filter(k -> !curData.containsKey(k))
            .forEach(wrapperProperties::remove);
      }
      updated.forEach((key, value) -> {
        if (wrapperProperties.containsKey(key)) {
          // Already created a wrapper, update it
          wrapperProperties.get(key).set(value);
        } else {
          // Create a new wrapper property
          SimpleObjectProperty<Object> wrapper = new SimpleObjectProperty<>(this, key, value);
          wrapper.addListener((o, p, newValue) -> setData(getData().put(key, newValue)));
          wrapperProperties.put(key, wrapper);
        }
      });
    });

    wrapperProperties.addListener((MapChangeListener<String, ObjectProperty<? super Object>>) change -> {
      if (change.wasAdded()) {
        propertySheet.getItems().add(new WidgetPropertySheet.PropertyItem<>(change.getValueAdded(), change.getKey()));
      } else if (change.wasRemoved()) {
        propertySheet.getItems().removeIf(i -> i.getName().equals(change.getKey()));
      }
      propertySheet.getItems().sort(itemSorter);
    });

    exportProperties(propertySheet.searchBoxVisibleProperty());
  }

  @Override
  public Pane getView() {
    return root;
  }

}
