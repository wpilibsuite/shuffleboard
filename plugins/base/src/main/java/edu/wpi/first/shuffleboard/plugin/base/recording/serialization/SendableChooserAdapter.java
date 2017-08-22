package edu.wpi.first.shuffleboard.plugin.base.recording.serialization;

import com.google.common.primitives.Bytes;

import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.plugin.base.data.SendableChooserData;
import edu.wpi.first.shuffleboard.plugin.base.data.types.SendableChooserType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.StringArrayType;
import edu.wpi.first.shuffleboard.plugin.base.data.types.StringType;

public final class SendableChooserAdapter extends TypeAdapter<SendableChooserData> {

  public SendableChooserAdapter() {
    super(new SendableChooserType());
  }

  @Override
  public SendableChooserData deserialize(byte[] buffer, int bufferPosition) {
    int cursor = bufferPosition;
    final String[] options = Serialization.decode(buffer, cursor, new StringArrayType());
    cursor += Serialization.sizeOfStringArray(options);
    final String defaultOption = Serialization.decode(buffer, cursor, new StringType());
    cursor += Serialization.SIZE_OF_INT + defaultOption.length();
    final String selectedOption = Serialization.decode(buffer, cursor, new StringType());
    return new SendableChooserData(options, defaultOption, selectedOption);
  }

  @Override
  public int getSerializedSize(SendableChooserData value) {
    return Serialization.sizeOfStringArray(value.getOptions())
        + Serialization.SIZE_OF_INT + value.getDefaultOption().length()
        + Serialization.SIZE_OF_INT + value.getSelectedOption().length();
  }

  @Override
  public byte[] serialize(SendableChooserData data) {
    final String[] options = data.getOptions();
    final String defaultOption = data.getDefaultOption();
    final String selectedOption = data.getSelectedOption();
    return Bytes.concat(
        Serialization.encode(options),
        Serialization.encode(defaultOption),
        Serialization.encode(selectedOption));
  }

}
