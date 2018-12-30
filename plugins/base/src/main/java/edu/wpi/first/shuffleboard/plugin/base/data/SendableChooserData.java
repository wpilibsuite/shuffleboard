package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.util.Maps;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Represents data options sent by the robot that may be selected by the drivers.
 */
public final class SendableChooserData extends ComplexData<SendableChooserData> {

  public static final String OPTIONS_KEY = "options";
  public static final String DEFAULT_OPTION_KEY = "default";
  public static final String SELECTED_OPTION_KEY = "selected";
  public static final String ACTIVE_OPTION_KEY = "active";

  private final String[] options;
  private final String defaultOption;
  private final String selectedOption;
  private final String activeOption;

  /**
   * Creates a new sendable chooser data object backed by the given map.
   *
   * @param map the map backing the data
   */
  public SendableChooserData(Map<String, Object> map) {
    this((String[]) map.getOrDefault(OPTIONS_KEY, new String[0]),
        (String) map.getOrDefault(DEFAULT_OPTION_KEY, ""),
        (String) map.getOrDefault(SELECTED_OPTION_KEY, ""),
        (String) map.getOrDefault(ACTIVE_OPTION_KEY, ""));
  }

  @SuppressWarnings("JavadocMethod")
  public SendableChooserData(String[] options, String defaultOption, String selectedOption, String activeOption) {
    this.options = Objects.requireNonNull(options, "options").clone();
    this.defaultOption = Objects.requireNonNull(defaultOption, "defaultOption");
    this.selectedOption = Objects.requireNonNull(selectedOption, "selectedOption");
    this.activeOption = Objects.requireNonNull(activeOption, activeOption);
  }

  public String[] getOptions() {
    return options.clone();
  }

  public String getDefaultOption() {
    return defaultOption;
  }

  public String getSelectedOption() {
    return selectedOption;
  }

  public String getActiveOption() {
    return activeOption;
  }

  public SendableChooserData withOptions(String... options) {
    return new SendableChooserData(options, this.defaultOption, this.selectedOption, activeOption);
  }

  public SendableChooserData withDefaultOption(String defaultOption) {
    return new SendableChooserData(this.options, defaultOption, this.selectedOption, activeOption);
  }

  public SendableChooserData withSelectedOption(String selectedOption) {
    return new SendableChooserData(this.options, this.defaultOption, selectedOption, activeOption);
  }

  @Override
  public String toString() {
    return String.format("SendableChooserData(options=%s, defaultOption=%s, selectedOption=%s, activeOption=%s)",
        Arrays.toString(options), defaultOption, selectedOption, activeOption);
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.<String, Object>builder()
        .put(OPTIONS_KEY, getOptions())
        .put(DEFAULT_OPTION_KEY, getDefaultOption())
        .put(SELECTED_OPTION_KEY, getSelectedOption())
        .put(ACTIVE_OPTION_KEY, getActiveOption())
        .build();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    final SendableChooserData that = (SendableChooserData) obj;

    return Arrays.equals(this.options, that.options)
        && this.defaultOption.equals(that.defaultOption)
        && this.selectedOption.equals(that.selectedOption)
        && this.activeOption.equals(that.activeOption);
  }

  @Override
  public int hashCode() {
    int result = Arrays.hashCode(options);
    result = 31 * result + defaultOption.hashCode();
    result = 31 * result + selectedOption.hashCode();
    result = 31 * result + activeOption.hashCode();
    return result;
  }

  @Override
  public String toHumanReadableString() {
    return String.format(
        "options=%s, selectedOption=%s, activeOption=%s",
        Arrays.toString(options),
        selectedOption,
        activeOption
    );
  }
}
