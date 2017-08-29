package edu.wpi.first.shuffleboard.plugin.cameraserver.recording.serialization;

import com.google.common.primitives.UnsignedBytes;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import javafx.scene.image.Image;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.WritableImage;

import static org.opencv.core.CvType.CV_8S;
import static org.opencv.core.CvType.CV_8U;
import static org.opencv.imgproc.Imgproc.INTER_CUBIC;

/**
 * Utility class for creating a JavaFX image from an OpenCV image.  This used by the preview views
 * to render an image in the GUI.
 */
public final class ImageConverter {

  private static final PixelFormat<IntBuffer> argbPixelFormat = PixelFormat.getIntArgbInstance();
  private WritableImage image;
  private IntBuffer pixels;
  private final MatOfByte imageBuffer = new MatOfByte();

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

    Mat toRender;
    if (mat.rows() > desiredHeight) {
      // Scale the image down
      toRender = new Mat();
      Imgproc.resize(
          mat, toRender,
          new Size((int) (((double) mat.cols() * desiredHeight) / mat.rows()), desiredHeight),
          0, 0, INTER_CUBIC
      );
    } else {
      toRender = mat;
    }

    final int width = toRender.cols();
    final int height = toRender.rows();


    // If the size of the Mat changed for whatever reason, allocate a new image with the proper
    // dimensions and a buffer big enough to hold all of the pixels in the image.
    if (image == null || image.getWidth() != width || image.getHeight() != height) {
      image = new WritableImage(width, height);
      pixels = IntBuffer.allocate(width * height);
    }

    // Copy the data from the image into a MatOfByte so we can extract the raw byte information
    Imgcodecs.imencode(".bmp", toRender, imageBuffer);

    final ByteBuffer buffer = ByteBuffer.wrap(imageBuffer.toArray());
    final int stride = buffer.capacity() / height;

    // Convert the data from the Mat into ARGB data that we can put into a JavaFX WritableImage
    switch (channels) {
      case 1:
        // 1 channel - convert grayscale to ARGB
        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            final int value = UnsignedBytes.toInt(buffer.get(stride * y + channels * x));
            pixels.put(width * (height - y - 1) + x, (0xff << 24) | (value << 16) | (value << 8) | value);
          }
        }

        break;

      case 3:
        // 3 channels - convert BGR to RGBA
        for (int y = 0; y < height; y++) {
          for (int x = 0; x < width; x++) {
            final int b = UnsignedBytes.toInt(buffer.get(stride * y + channels * x));
            final int g = UnsignedBytes.toInt(buffer.get(stride * y + channels * x + 1));
            final int r = UnsignedBytes.toInt(buffer.get(stride * y + channels * x + 2));
            pixels.put(width * (height - y - 1) + x, (0xff << 24) | (r << 16) | (g << 8) | b);
          }
        }

        break;
      default:
        throw new UnsupportedOperationException("Only 1 or 3 channel images can be shown, tried "
            + "to show a " + channels + " channel image");
    }

    image.getPixelWriter().setPixels(0, 0, width, height, argbPixelFormat, pixels, width);

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
