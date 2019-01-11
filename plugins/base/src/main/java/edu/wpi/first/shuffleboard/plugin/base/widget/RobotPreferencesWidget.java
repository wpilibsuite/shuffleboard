package edu.wpi.first.shuffleboard.plugin.base.widget;

import edu.wpi.first.shuffleboard.api.components.ExtendedPropertySheet;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.sources.DataSourceUtils;
import edu.wpi.first.shuffleboard.api.util.AlphanumComparator;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.ParametrizedController;
import edu.wpi.first.shuffleboard.api.widget.SimpleAnnotatedWidget;
import edu.wpi.first.shuffleboard.plugin.base.data.RobotPreferencesData;

import com.google.common.collect.ImmutableList;

import org.controlsfx.control.PropertySheet;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
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
      Comparator.comparing(i -> i.getName().toLowerCase(Locale.US), AlphanumComparator.INSTANCE);

  @FXML
  private void initialize() {
    propertySheet.setPropertyEditorFactory(ExtendedPropertySheet.CUSTOM_EDITOR_FACTORY);
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
        if (DataSourceUtils.isNotMetadata(key)) {
          wrapperProperties.computeIfAbsent(key, k -> generateWrapper(k, value)).setValue(value);
        }
      });
    });

    wrapperProperties.addListener((MapChangeListener<String, ObjectProperty<? super Object>>) change -> {
      if (change.wasAdded()) {
        propertySheet.getItems().add(new ExtendedPropertySheet.PropertyItem<>(change.getValueAdded(), change.getKey()));
      } else if (change.wasRemoved()) {
        propertySheet.getItems().removeIf(i -> i.getName().equals(change.getKey()));
      }
      propertySheet.getItems().sort(itemSorter);
    });
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of(
        Group.of("Miscellaneous",
            Setting.of("Show search box", propertySheet.searchBoxVisibleProperty(), Boolean.class)
        )
    );
  }

  @Override
  public Pane getView() {
    return root;
  }

  private <T> SimpleObjectProperty<T> generateWrapper(String key, T initialValue) {
    SimpleObjectProperty<T> wrapper = new SimpleObjectProperty<>(this, key, initialValue);
    wrapper.addListener((__, prev, value) -> setData(dataOrDefault.get().put(key, value)));
    return wrapper;
  }

}
