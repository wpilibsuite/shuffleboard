package edu.wpi.first.shuffleboard.data.types;

import edu.wpi.first.shuffleboard.data.ComplexDataType;
import edu.wpi.first.shuffleboard.data.SendableChooserData;

import java.util.Collections;
import java.util.function.Function;

public class SendableChooserType implements ComplexDataType<SendableChooserData> {

  @Override
  public Function<java.util.Map<String, Object>, SendableChooserData> fromMap() {
    return SendableChooserData::new;
  }

  @Override
  public SendableChooserData getDefaultValue() {
    return new SendableChooserData(Collections.emptyMap());
  }

  @Override
  public String getName() {
    return "SendableChooser";
  }
}
