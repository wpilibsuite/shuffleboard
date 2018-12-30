package edu.wpi.first.shuffleboard.plugin.base.data;

import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.util.Map;

public class CommandData extends ComplexData<CommandData> {

  private final String name;
  private final boolean running;
  private final boolean isParented;

  /**
   * Creates a new CommandData object.
   *
   * @param name       the name of the command
   * @param running    if the command is currently running
   * @param isParented if the command has a parent (i.e. is part of a command group)
   */
  public CommandData(String name, boolean running, boolean isParented) {
    this.name = name;
    this.running = running;
    this.isParented = isParented;
  }

  /**
   * Creates a new CommandData object from a map.
   *
   * @param map the map of property names to values to create a data object from
   */
  public CommandData(Map<String, Object> map) {
    this(
        (String) map.getOrDefault(".name", ""),
        (Boolean) map.getOrDefault("running", false),
        (Boolean) map.getOrDefault("isParented", false)
    );
  }

  public CommandData withRunning(boolean running) {
    return new CommandData(this.name, running, this.isParented);
  }

  @Override
  public Map<String, Object> asMap() {
    return ImmutableMap.of(
        "name", name,
        "running", running,
        "isParented", isParented
    );
  }

  public String getName() {
    return name;
  }

  public boolean isRunning() {
    return running;
  }

  public boolean isParented() {
    return isParented;
  }

  @Override
  public String toHumanReadableString() {
    return "Running: " + running;
  }
}
