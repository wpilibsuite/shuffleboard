package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataType;

import java.io.File;

/**
 * A TypeAdapter combines {@link Serializer} and {@link Deserializer} in one class for ease of use.
 *
 * @param <T> the type of data that be serialized and deserialized
 */
public abstract class TypeAdapter<T> implements Serializer<T>, Deserializer<T> {

  private final DataType<T> dataType;
  private File currentFile = null;

  /**
   * Creates a new adapter for the given data type.
   *
   * @param dataType the type of the data that can be serialized and deserialized
   */
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

  /**
   * Gets the current recording file being loaded. This is useful for adapters that record extra data in separate files
   * (e.g. a camera stream may save a video file alongside the .sbr recording file).
   */
  public final File getCurrentFile() {
    return currentFile;
  }

  /**
   * Sets the current recording file. This is used by the playback mechanism to allow type adapters to know what
   * recording file is being loaded.
   */
  public final void setCurrentFile(File currentFile) {
    this.currentFile = currentFile;
  }

}
