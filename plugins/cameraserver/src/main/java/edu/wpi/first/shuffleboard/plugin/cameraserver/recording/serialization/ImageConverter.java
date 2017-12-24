package edu.wpi.first.shuffleboard.plugin.cameraserver.recording.serialization;

import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import static org.opencv.core.CvType.CV_8S;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2RGB;
import static org.opencv.imgproc.Imgproc.COLOR_GRAY2RGB;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;

/**
 * Utility class for creating a JavaFX image from an OpenCV image.  This used by the preview views
 * to render an image in the GUI.
 */
public final class ImageConverter {

  private static final PixelFormat<IntBuffer> argbPixelFormat = PixelFormat.getIntArgbInstance();
  private WritableImage image;
  private byte[] data;
  private ByteBuffer buffer;

  /**
   * Convert a BGR-formatted OpenCV {@link Mat} into a JavaFX {@link Image}. JavaFX understands ARGB
   * pixel data, so one way to turn a Mat into a JavaFX image is to shift around the bytes from the
   * Mat into an int array of pixels.
   *
   * @param mat           An 8-bit OpenCV Mat containing an image with either 1 or 3 channels
   * @param desiredHeight the desired height of the image
   *
   * @return A JavaFX image, or null for empty
   */
  public Image convert(Mat mat, int desiredHeight) {

    final int channels = mat.channels();

    assert channels == 3 || channels == 1 :
        "Only 3-channel BGR images or single-channel grayscale images can be converted";

    assert mat.depth() == CV_8U || mat.depth() == CV_8S :
        "Only images with 8 bits per channel can be converted";

    // Don't try to render empty images.
    if (mat.empty()) {
      return null;
    }

    Mat toRender = new Mat();
    if (mat.rows() > desiredHeight) {
      // Scale the image down
      Imgproc.resize(
          mat, toRender,
          new Size((int) (((double) mat.cols() * desiredHeight) / mat.rows()), desiredHeight),
          0, 0, INTER_CUBIC
      );
    } else {
      mat.copyTo(toRender);
    }

    final int width = toRender.cols();
    final int height = toRender.rows();


    // If the size of the Mat changed for whatever reason, allocate a new image with the proper
    // dimensions and a buffer big enough to hold all of the pixels in the image.
    if (image == null || image.getWidth() != width || image.getHeight() != height) {
      image = new WritableImage(width, height);
      data = new byte[width * height * channels];
      buffer = ByteBuffer.wrap(data);
    }

    // Convert BGR to RGB so it can be rendered  easily
    switch (channels) {
      case 1:
        Imgproc.cvtColor(toRender, toRender, COLOR_GRAY2RGB);
        break;
      case 3:
        Imgproc.cvtColor(toRender, toRender, COLOR_BGR2RGB);
        break;
      default:
        throw new UnsupportedOperationException("Only 1 or 3-channel images are supported");
    }

    // Grab the image data
    toRender.get(0, 0, data);

    image.getPixelWriter().setPixels(0, 0, width, height, PixelFormat.getByteRgbInstance(), buffer, width * channels);

    return image;
  }

  /**
   * Convert a BGR-formatted OpenCV {@link Mat} into a JavaFX {@link Image}. JavaFX understands ARGB
   * pixel data, so one way to turn a Mat into a JavaFX image is to shift around the bytes from the
   * Mat into an int array of pixels.
   *
   * @param mat An 8-bit OpenCV Mat containing an image with either 1 or 3 channels
   *
   * @return A JavaFX image, or null for empty
   */
  public Image convert(Mat mat) {
    return convert(mat, mat.rows());
  }

}
