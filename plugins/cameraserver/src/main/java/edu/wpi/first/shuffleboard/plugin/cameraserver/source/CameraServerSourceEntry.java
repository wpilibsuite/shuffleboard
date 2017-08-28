package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.sources.SourceEntry;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;

public class CameraServerSourceEntry implements SourceEntry {

  private final CameraServerData data;

  public CameraServerSourceEntry(CameraServerData data) {
    this.data = data;
  }

  @Override
  public String getName() {
    return data.getName();
  }

  @Override
  public Object getValue() {
    return data;
  }

  @Override
  public Object getValueView() {
    return data.getImage();
  }

  @Override
  public CameraServerSource get() {
    return new CameraServerSource(getName());
  }

}
