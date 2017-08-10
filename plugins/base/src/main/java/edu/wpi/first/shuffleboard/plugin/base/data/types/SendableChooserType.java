package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.SendableChooserData;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class SendableChooserType extends ComplexDataType<SendableChooserData> {

  public SendableChooserType() {
    super("SendableChooser", SendableChooserData.class);
  }

  @Override
  public Function<Map<String, Object>, SendableChooserData> fromMap() {
    return SendableChooserData::new;
  }

  @Override
  public SendableChooserData getDefaultValue() {
    return new SendableChooserData(Collections.emptyMap());
  }

}
