package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.prefs.Group;

import java.util.List;

public interface SettingsHolder {

  /**
   * Gets the settings for this component. Settings are defined in groups. There may be arbitrarily many groups or
   * settings per group.
   *
   * <p>General structure:
   * <pre>
   * {@code
   * List.of(
   *   Group.of("Group Name",
   *     Setting.of("Setting name", "Setting description", settingProperty, TypeOfSetting.class)
   *   )
   * );
   * }
   * </pre>
   */
  List<Group> getSettings();

}
