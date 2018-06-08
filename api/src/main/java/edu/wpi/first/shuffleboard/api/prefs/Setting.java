package edu.wpi.first.shuffleboard.api.prefs;

import java.util.Objects;

import javafx.beans.property.Property;

/**
 * A single user-configurable setting.
 *
 * @param <T> the type of the value being configured
 */
public final class Setting<T> {

  private final Property<T> property;
  private final String name;
  private final String description;

  /**
   * Creates a new setting.
   *
   * @param name        the name of the setting. This cannot be null or empty
   * @param description a description of the setting. This may be null or empty
   * @param property    the property to configure
   * @param <T>         the type of the value to configure
   *
   * @return a new setting
   */
  public static <T> Setting<T> of(String name, String description, Property<T> property) {
    return new Setting<>(name, description, property);
  }

  /**
   * Creates a new setting with no description.
   *
   * @param name     the name of the setting
   * @param property the property to configure
   * @param <T>      the type of the value to configure
   *
   * @return a new setting
   */
  public static <T> Setting<T> of(String name, Property<T> property) {
    return new Setting<>(name, null, property);
  }

  private Setting(String name, String description, Property<T> property) {
    Objects.requireNonNull(name, "A setting name cannot be null");
    if (name.chars().allMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException("A setting name cannot be empty");
    }
    Objects.requireNonNull(property, "A setting's property cannot be null");
    this.name = name;
    this.description = description;
    this.property = property;
  }

  public Property<T> getProperty() {
    return property;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }
}
