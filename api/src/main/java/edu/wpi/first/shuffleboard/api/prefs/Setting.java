package edu.wpi.first.shuffleboard.api.prefs;

import java.util.Objects;

import javafx.beans.property.Property;
import javafx.beans.property.ReadOnlyProperty;

/**
 * A single user-configurable setting.
 *
 * @param <T> the type of the value being configured
 */
public final class Setting<T> {

  private final Property<T> property;
  private final String name;
  private final String description;
  private final Class<? extends T> type;

  /**
   * Creates a new setting. Note: custom widgets and components should use one of the typed factory methods to allow
   * their properties to be set from a remote definition (such as the program of an FRC robot).
   *
   * @param name        the name of the setting. This cannot be null or empty
   * @param description a description of the setting. This may be null or empty
   * @param property    the property to configure
   * @param <T>         the type of the value to configure
   *
   * @return a new setting
   *
   * @see #of(String, String, Property, Class)
   */
  public static <T> Setting<T> of(String name, String description, Property<T> property) {
    return of(name, description, property, null);
  }

  /**
   * Creates a new setting.
   *
   * @param name        the name of the setting. This cannot be null or empty
   * @param description a description of the setting. This may be null or empty
   * @param property    the property to configure
   * @param type        the type of values accepted by this setting
   * @param <T>         the type of the value to configure
   *
   * @return a new setting
   */
  public static <T> Setting<T> of(String name, String description, Property<T> property, Class<? extends T> type) {
    return new Setting<>(name, description, property, type);
  }

  /**
   * Creates a new setting with no description. Note: custom widgets and components should use one of the typed factory
   * methods to allow their properties to be set from a remote definition (such as the program of an FRC robot).
   *
   * @param name     the name of the setting
   * @param property the property to configure
   * @param <T>      the type of the value to configure
   *
   * @return a new setting
   *
   * @see #of(String, Property, Class)
   */
  public static <T> Setting<T> of(String name, Property<T> property) {
    return of(name, null, property, null);
  }

  /**
   * Creates a new setting with no description.
   *
   * @param name     the name of the setting. This cannot be null or empty
   * @param property the property to configure
   * @param type     the type of values accepted by this setting
   * @param <T>      the type of the value to configure
   *
   * @return a new setting
   */
  public static <T> Setting<T> of(String name, Property<T> property, Class<? extends T> type) {
    return of(name, null, property, type);
  }

  private Setting(String name, String description, Property<T> property, Class<? extends T> type) {
    Objects.requireNonNull(name, "A setting name cannot be null");
    if (name.chars().allMatch(Character::isWhitespace)) {
      throw new IllegalArgumentException("A setting name cannot be empty");
    }
    Objects.requireNonNull(property, "A setting's property cannot be null");
    this.name = name;
    this.description = description;
    this.property = property;
    this.type = type;
  }

  /**
   * Sets the value of this setting.
   *
   * @param value the new value
   *
   * @throws IllegalArgumentException if the given value is incompatible with the {@link #getType() type} of this
   *                                  setting
   */
  public void setValue(T value) {
    if (type != null && !type.isInstance(value)) {
      throw new IllegalArgumentException(
          String.format("Value is not a %s: '%s' (is: %s)", type.getName(), value, value.getClass().getName()));
    }
    property.setValue(value);
  }

  public ReadOnlyProperty<T> getProperty() {
    return property;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Gets the type of allowable values. This may be null: if so, there is no restriction.
   */
  public Class<? extends T> getType() {
    return type;
  }
}
