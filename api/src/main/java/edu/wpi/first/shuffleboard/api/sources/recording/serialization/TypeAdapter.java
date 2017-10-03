package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataType;

import java.io.File;

public abstract class TypeAdapter<T> implements Serializer<T>, Deserializer<T> {

  private final DataType<T> dataType;
  private File currentFile = null;

  protected TypeAdapter(DataType<T> dataType) {
    this.dataType = dataType;
  }

  @Override
  public final DataType<T> getDataType() {
    return dataType;
  }

  /**
   * Resets the state of this type adapter (if it has state) before a new recording starts.
   */
  public void cleanUp() { //NOPMD empty abstract method body
    // Default to NOP
  }

  public final File getCurrentFile() {
    return currentFile;
  }

  public final void setCurrentFile(File currentFile) {
    this.currentFile = currentFile;
  }

}
