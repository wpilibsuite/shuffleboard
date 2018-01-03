package edu.wpi.first.shuffleboard.api.data;

/**
 * An exception thrown when attempting to create a data object without having all the variables available.
 */
public class IncompleteDataException extends RuntimeException {

  public IncompleteDataException(String message) {
    super(message);
  }

  public IncompleteDataException(String message, Throwable cause) {
    super(message, cause);
  }

}
