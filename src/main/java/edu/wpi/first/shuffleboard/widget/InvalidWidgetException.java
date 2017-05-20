package edu.wpi.first.shuffleboard.widget;

/**
 * An exception thrown when a widget is determined to have an invalid configuration.
 */
public class InvalidWidgetException extends RuntimeException {

  /**
   * @inheritDoc
   */
  public InvalidWidgetException(String message) {
    super(message);
  }

}
