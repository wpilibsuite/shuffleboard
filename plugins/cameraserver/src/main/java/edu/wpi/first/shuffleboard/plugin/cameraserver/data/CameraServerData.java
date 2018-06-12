package edu.wpi.first.shuffleboard.plugin.cameraserver.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

import com.google.common.collect.ImmutableMap;

import org.opencv.core.Mat;

import java.util.Map;

public class CameraServerData extends ComplexData<CameraServerData> {

  private final String name;
  private final Mat image;
  private final double fps;
  private final double bandwidth;

  /**
   * Creates a new data object.
   *
   * @param name      the name of the camera
   * @param image     the images being supplied by the stream, or <tt>null</tt> if the stream is not providing an image
   * @param fps       the current FPS of the stream. If the FPS is unknown, set to -1
   * @param bandwidth the current bandwidth of the stream, in bytes per second. If the bandwidth is unknown, set to -1
   */
  public CameraServerData(String name, Mat image, double fps, double bandwidth) {
    this.name = name;
    this.image = image;
    this.fps = fps;
    this.bandwidth = bandwidth;
  }

  public String getName() {
    return name;
  }

  public Mat getImage() {
    return image;
  }

  /**
   * Gets the current framerate of the stream in frames per second.
   */
  public double getFps() {
    return fps;
  }

  /**
   * Gets the current bandwidth use of the stream in bytes per second.
   */
  public double getBandwidth() {
    return bandwidth;
  }

  @Override
  public Map<String, Object> asMap() {
    return ImmutableMap.of(
        "name", name,
        "image", image,
        "fps", fps,
        "bandwidth", bandwidth
    );
  }

  /**
   * Creates a new data object identical to this one, but with a different image.
   *
   * @param image the image for the new data object
   */
  public CameraServerData withImage(Mat image) {
    return new CameraServerData(name, image, fps, bandwidth);
  }

}
