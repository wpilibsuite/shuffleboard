package edu.wpi.first.shuffleboard.plugin.cameraserver.data;

import com.google.common.collect.ImmutableMap;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.io.Serializable;
import java.util.Map;

import javafx.scene.image.Image;

// serialversion UID doesn't matter since this is only used in one JVM at a time
@SuppressFBWarnings("SE_NO_SERIALVERSIONID")
public final class CameraServerData extends ComplexData<CameraServerData> implements Serializable {

  private final String name;
  private final transient Image image;

  public CameraServerData(String name, Image image) {
    this.name = name;
    this.image = image;
  }

  public String getName() {
    return name;
  }

  public Image getImage() {
    return image;
  }

  @Override
  public Map<String, Object> asMap() {
    return ImmutableMap.of(
        "name", name,
        "image", image
    );
  }

  /**
   * Creates a new data object identical to this one, but with a different image.
   *
   * @param image the image for the new data object
   */
  public CameraServerData withImage(Image image) {
    return new CameraServerData(name, image);
  }

}
