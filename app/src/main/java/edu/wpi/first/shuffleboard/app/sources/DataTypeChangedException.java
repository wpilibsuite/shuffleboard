package edu.wpi.first.shuffleboard.app.sources;

/**
 * An exception thrown when the data type of a restored source is different from the type when it was destroyed.
 */
public class DataTypeChangedException extends RuntimeException {

  public DataTypeChangedException(String message) {
    super(message);
  }

  public DataTypeChangedException(String message, Throwable cause) {
    super(message, cause);
  }

}
