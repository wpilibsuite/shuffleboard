package edu.wpi.first.shuffleboard.app;

import javafx.stage.Window;

/**
 * An immutable data object that contains the position and size of a window.
 */
public final class WindowGeometry {

  private final double x;
  private final double y;
  private final double width;
  private final double height;

  /**
   * Creates a new data object from a window.
   *
   * @param window the window
   */
  public WindowGeometry(Window window) {
    this.x = window.getX();
    this.y = window.getY();
    this.width = window.getWidth();
    this.height = window.getHeight();
  }

  public double getX() {
    return x;
  }

  public double getY() {
    return y;
  }

  public double getWidth() {
    return width;
  }

  public double getHeight() {
    return height;
  }
}
