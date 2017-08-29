package edu.wpi.first.shuffleboard.api.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * An implementation of {@code ImageView} that has proper resizing code. The default implementation tends to not
 * respect the fitWidth/fitHeight properties if those properties are bound to the size of a parent container.
 *
 * <p>This code is taken from <a href="https://stackoverflow.com/a/35202191">https://stackoverflow.com/a/35202191</a>
 */
public class ResizableImageView extends ImageView {

  public ResizableImageView() {
    setPreserveRatio(false);
  }

  @Override
  public double minWidth(double height) {
    return 1;
  }

  @Override
  public double prefWidth(double height) {
    Image image = getImage();
    if (image == null) {
      return minWidth(height);
    }
    return image.getWidth();
  }

  @Override
  public double maxWidth(double height) {
    return Short.MAX_VALUE;
  }

  @Override
  public double minHeight(double width) {
    return 1;
  }

  @Override
  public double prefHeight(double width) {
    Image image = getImage();
    if (image == null) {
      return minHeight(width);
    }
    return image.getHeight();
  }

  @Override
  public double maxHeight(double width) {
    return Short.MAX_VALUE;
  }

  @Override
  public boolean isResizable() {
    return true;
  }

  @Override
  public void resize(double width, double height) {
    setFitWidth(width);
    setFitHeight(height);
  }

}
