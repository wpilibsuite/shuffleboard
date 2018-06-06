package edu.wpi.first.shuffleboard.api.prefs;

import javafx.beans.property.Property;

public final class Setting<T> {

  private final Property<T> property;
  private final String name;
  private final String description;

  public static <T> Setting<T> of(String name, String description, Property<T> property) {
    return new Setting<>(name, description, property);
  }

  public static <T> Setting<T> of(String name, Property<T> property) {
    return new Setting<>(name, null, property);
  }

  private Setting(String name, String description, Property<T> property) {
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
