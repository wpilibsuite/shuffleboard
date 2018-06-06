package edu.wpi.first.shuffleboard.api.prefs;

import com.google.common.collect.ImmutableList;

import java.util.Collection;

public final class Group {

  private final String name;
  private final ImmutableList<Setting<?>> settings;

  public static Group of(String name, Setting<?>... settings) {
    return new Group(name, ImmutableList.copyOf(settings));
  }

  public static Group of(String name, Collection<Setting<?>> settings) {
    return new Group(name, ImmutableList.copyOf(settings));
  }

  private Group(String name, ImmutableList<Setting<?>> settings) {
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
