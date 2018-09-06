package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.CommandData;

import java.util.Map;
import java.util.function.Function;

public final class CommandType extends ComplexDataType<CommandData> {

  public static final CommandType Instance = new CommandType();

  private CommandType() {
    super("Command", CommandData.class);
  }

  @Override
  public Function<Map<String, Object>, CommandData> fromMap() {
    return CommandData::new;
  }

  @Override
  public CommandData getDefaultValue() {
    return new CommandData("Command", false, false);
  }

}
