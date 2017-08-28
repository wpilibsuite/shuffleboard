package edu.wpi.first.shuffleboard.plugin.cameraserver.data;

import com.google.common.collect.ImmutableMap;

import edu.wpi.first.shuffleboard.api.data.ComplexData;

import java.util.Map;

import javafx.scene.image.Image;

public final class CameraServerData extends ComplexData<CameraServerData> {

  private final String name;
  private final Image image;

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
