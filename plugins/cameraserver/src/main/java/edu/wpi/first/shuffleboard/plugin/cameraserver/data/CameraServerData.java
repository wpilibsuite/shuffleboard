package edu.wpi.first.shuffleboard.plugin.cameraserver.data;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

import com.google.common.collect.ImmutableMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.Serializable;
import java.util.Map;

import javafx.scene.image.Image;

// serialversion UID doesn't matter since this is only used in one JVM at a time
@SuppressFBWarnings("SE_NO_SERIALVERSIONID")
public final class CameraServerData extends ComplexData<CameraServerData> implements Serializable {

  private final String name;
  private final transient Image image;
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
  public CameraServerData(String name, Image image, double fps, double bandwidth) {
    this.name = name;
    this.image = image;
    this.fps = fps;
    this.bandwidth = bandwidth;
  }

  public String getName() {
    return name;
  }

  public Image getImage() {
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
  public CameraServerData withImage(Image image) {
    return new CameraServerData(name, image, fps, bandwidth);
  }

}
