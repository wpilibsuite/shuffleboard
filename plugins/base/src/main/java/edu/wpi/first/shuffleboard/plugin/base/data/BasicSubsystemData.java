package edu.wpi.first.shuffleboard.plugin.base.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.util.Maps;

import java.util.Map;
import java.util.Objects;

public final class BasicSubsystemData extends ComplexData<BasicSubsystemData> {

  private final boolean hasDefaultCommand;
  private final String defaultCommandName;
  private final boolean hasCommand;
  private final String currentCommandName;

  /**
   * Creates a new data object for basic subsystem data.
   *
   * @param hasDefaultCommand  flag marking this subsystem as having a default command
   * @param defaultCommandName the name of the default command
   * @param hasCommand         true if a command is running that requires this subsystem, otherwise false
   * @param currentCommandName the name of the current command running on this subsystem
   */
  public BasicSubsystemData(boolean hasDefaultCommand,
                            String defaultCommandName,
                            boolean hasCommand,
                            String currentCommandName) {
    this.hasDefaultCommand = hasDefaultCommand;
    this.defaultCommandName = hasDefaultCommand ? defaultCommandName : "";
    this.hasCommand = hasCommand;
    this.currentCommandName = hasCommand ? currentCommandName : "";
  }

  /**
   * Creates a new data object from a map.
   */
  public BasicSubsystemData(Map<String, Object> map) {
    this((Boolean) map.getOrDefault(".hasDefault", false),
        (String) map.getOrDefault(".default", ""),
        (Boolean) map.getOrDefault(".hasCommand", false),
        (String) map.getOrDefault(".command", ""));
  }

  @Override
  public Map<String, Object> asMap() {
    return Maps.<String, Object>builder()
        .put(".hasDefault", hasDefaultCommand)
        .put(".default", defaultCommandName)
        .put(".hasCommand", hasCommand)
        .put(".command", currentCommandName)
        .build();
  }

  public boolean hasDefaultCommand() {
    return hasDefaultCommand;
  }

  public String getDefaultCommandName() {
    return defaultCommandName;
  }

  public boolean hasCommand() {
    return hasCommand;
  }

  public String getCurrentCommandName() {
    return currentCommandName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BasicSubsystemData that = (BasicSubsystemData) o;
    return this.hasDefaultCommand == that.hasDefaultCommand
        && this.defaultCommandName.equals(that.defaultCommandName)
        && this.hasCommand == that.hasCommand
        && this.currentCommandName.equals(that.currentCommandName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hasDefaultCommand, defaultCommandName, hasCommand, currentCommandName);
  }

  @Override
  public String toString() {
    return String.format(
        "BasicSubsystemData(hasDefaultCommand=%s, defaultCommandName='%s', hasCommand=%s, currentCommandName='%s')",
        hasDefaultCommand, defaultCommandName, hasCommand, currentCommandName);
  }

}
