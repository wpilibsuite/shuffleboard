package edu.wpi.first.shuffleboard.api.widget;

/**
 * An exception thrown when a widget is determined to have an invalid configuration.
 */
public class InvalidWidgetException extends RuntimeException {

  /**
   * Constructs an exception with the given message.
   */
  public InvalidWidgetException(String message) {
    super(message);
  }

}
