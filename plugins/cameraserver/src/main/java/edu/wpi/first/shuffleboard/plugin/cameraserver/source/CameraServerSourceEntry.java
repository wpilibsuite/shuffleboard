package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.sources.SourceEntry;

public class CameraServerSourceEntry implements SourceEntry {

  private final String name;

  public CameraServerSourceEntry(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return "/" + name;
  }

  @Override
  public String getViewName() {
    return name;
  }

  @Override
  public Object getValue() {
    return name;
  }

  @Override
  public Object getValueView() {
    return null;
  }

  @Override
  public CameraServerSource get() {
    return CameraServerSourceType.INSTANCE.forName(name);
  }

}
