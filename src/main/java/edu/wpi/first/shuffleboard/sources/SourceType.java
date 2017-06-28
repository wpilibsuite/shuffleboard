package edu.wpi.first.shuffleboard.sources;

public enum SourceType {

  NONE(false, ""),
  NETWORK_TABLE(true, "network_table://"),
  CAMERA_SERVER(true, "camera_server://");

  public final boolean isRecordable;
  private final String protocol;

  SourceType(boolean isRecordable, String protocol) {
    this.isRecordable = isRecordable;
    this.protocol = protocol;
  }

  public String toUri(String sourceName) {
    return protocol + sourceName;
  }

  public String getProtocol() {
    return protocol;
  }

}
