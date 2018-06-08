package edu.wpi.first.shuffleboard.api.prefs;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.Objects;

/**
 * A group of {@link Setting Settings}.
 */
public final class Group {

  private final String name;
  private final ImmutableList<Setting<?>> settings;

  /**
   * Creates a new group of settings.
   *
   * @param name     the name of the group
   * @param settings the settings in the group
   *
   * @return a new group
   */
  public static Group of(String name, Setting<?>... settings) {
    return new Group(name, ImmutableList.copyOf(settings));
  }

  /**
   * Creates a new group of settings.
   *
   * @param name     the name of the group
   * @param settings the settings in the group
   *
   * @return a new group
   */
  public static Group of(String name, Collection<Setting<?>> settings) {
    return new Group(name, ImmutableList.copyOf(settings));
  }

  private Group(String name, ImmutableList<Setting<?>> settings) {
    Objects.requireNonNull(name, "A group name cannot be null");
    if (name.chars().allMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException("A group name cannot be empty");
    }
    this.name = name;
    this.settings = settings;
  }

  public String getName() {
    return name;
  }

  public ImmutableList<Setting<?>> getSettings() {
    return settings;
  }
}
