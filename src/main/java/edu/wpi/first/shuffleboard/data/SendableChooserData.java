package edu.wpi.first.shuffleboard.data;

import edu.wpi.first.shuffleboard.util.PropertyUtils;

import java.util.Arrays;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableMap;

/**
 * Represents data options sent by the robot that may be selected by the drivers.
 */
public class SendableChooserData extends ComplexData {

  static final String OPTIONS_KEY = "options";
  static final String DEFAULT_OPTION_KEY = "default";
  static final String SELECTED_OPTION_KEY = "selected";

  private final Property<String[]> options
      = new SimpleObjectProperty<>(this, "options", new String[0]);
  private final Property<String> defaultOption
      = new SimpleObjectProperty<>(this, "defaultOption", "");
  private final Property<String> selectedOption
      = new SimpleObjectProperty<>(this, "selectedOption", "");

  /**
   * Creates a new sendable chooser data object backed by the given map.
   *
   * @param map the map backing the data
   */
  public SendableChooserData(ObservableMap<String, Object> map) {
    super(map);
    PropertyUtils.bindToMapBidirectionally(options, map, OPTIONS_KEY, v -> (String[]) v);
    PropertyUtils.bindToMapBidirectionally(defaultOption, map, DEFAULT_OPTION_KEY, v -> (String) v);
    PropertyUtils.bindToMapBidirectionally(selectedOption, map, SELECTED_OPTION_KEY,
        v -> (String) v);
  }

  public String[] getOptions() {
    return options.getValue();
  }

  public Property<String[]> optionsProperty() {
    return options;
  }

  public void setOptions(String... options) {
    this.options.setValue(options);
  }

  public String getDefaultOption() {
    return defaultOption.getValue();
  }

  public Property<String> defaultOptionProperty() {
    return defaultOption;
  }

  public void setDefaultOption(String defaultOption) {
    this.defaultOption.setValue(defaultOption);
  }

  public String getSelectedOption() {
    return selectedOption.getValue();
  }

  public Property<String> selectedOptionProperty() {
    return selectedOption;
  }

  public void setSelectedOption(String selectedOption) {
    this.selectedOption.setValue(selectedOption);
  }

  @Override
  public String toString() {
    return String.format("SendableChooserData(options=%s, defaultOption=%s, selectedOption=%s)",
        Arrays.toString(getOptions()), getDefaultOption(), getSelectedOption());
  }
}
