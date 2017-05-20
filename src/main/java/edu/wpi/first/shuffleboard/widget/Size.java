package edu.wpi.first.shuffleboard.widget;

/**
 *
 */
public class Size implements Comparable<Size> {

  private final int width;
  private final int height;

  /**
   * Creates a size with the given width and height. These must both be positive values.
   */
  public Size(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public static Size of(int width, int height) {
    return new Size(width, height);
  }

  public int getWidth() {
    return width;
  }

  public int getHeight() {
    return height;
  }

  @Override
  public int compareTo(Size o) {
    if (width < o.width) {
      return -1;
    } else if (width > o.width) {
      return 1;
    } else if (height < o.height) {
      return -1;
    } else if (height > o.height) {
      return 1;
    } else {
      return 0;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Size size = (Size) o;

    if (width != size.width) return false;
    return height == size.height;
  }

  @Override
  public int hashCode() {
    int result = width;
    result = 31 * result + height;
    return result;
  }

  @Override
  public String toString() {
    return "Size(" +
        "width=" + width +
        ", height=" + height +
        ')';
  }
}
