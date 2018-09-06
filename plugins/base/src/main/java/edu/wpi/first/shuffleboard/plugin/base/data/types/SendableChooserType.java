package edu.wpi.first.shuffleboard.plugin.base.data.types;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.plugin.base.data.SendableChooserData;

import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public final class SendableChooserType extends ComplexDataType<SendableChooserData> {

  public static final SendableChooserType Instance = new SendableChooserType();

  private SendableChooserType() {
    super("String Chooser", SendableChooserData.class);
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
