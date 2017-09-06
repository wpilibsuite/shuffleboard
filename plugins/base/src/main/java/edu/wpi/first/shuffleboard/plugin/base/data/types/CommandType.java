package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.widget.CommandData;

import java.util.Map;
import java.util.function.Function;

public class CommandType extends ComplexDataType<CommandData> {

  public CommandType() {
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
