package edu.wpi.first.shuffleboard.plugin.cameraserver.data;

/**
 * Represents the resolution of an image.
 */
public final class Resolution {

  private final int width;
  private final int height;

  /**
   * An "empty" resolution of {@code 0 x 0}. This should be used instead of {@code null}.
   */
  public static final Resolution EMPTY = new Resolution(0, 0);

  /**
   * Creates a new resolution object.
   *
   * @param width  the width of the image
   * @param height the height of the image
   */
  public Resolution(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  @Override
  public String toString() {
    return width + "x" + height;
  }

}
