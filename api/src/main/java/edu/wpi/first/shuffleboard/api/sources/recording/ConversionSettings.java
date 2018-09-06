package edu.wpi.first.shuffleboard.api.sources.recording;

import java.util.Objects;

/**
 * A container object for various settings for converting binary Shuffleboard recordings to other formats.
 */
public final class ConversionSettings {

  private final boolean convertMetadata;

  /**
   * Creates a new container object.
   *
   * @param convertMetadata if metadata entries should be converted
   */
  public ConversionSettings(boolean convertMetadata) {
    this.convertMetadata = convertMetadata;
  }

  /**
   * Checks if metadata entries should be converted.
   *
   * @return true if metadata entries should be converted, false if not
   */
  public boolean isConvertMetadata() {
    return convertMetadata;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConversionSettings that = (ConversionSettings) o;
    return this.convertMetadata == that.convertMetadata;
  }

  @Override
  public int hashCode() {
    return Objects.hash(convertMetadata);
  }

  @Override
  public String toString() {
    return String.format("ConversionSettings(convertMetadata=%s)", convertMetadata);
  }

}
