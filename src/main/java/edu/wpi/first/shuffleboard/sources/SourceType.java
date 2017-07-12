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

  /**
   * Removes the protocol prefix from a string. Has no effect if the given text does not start with this types protocol.
   *
   * @param text the text to remove the protocol string from
   */
  public String removeProtocol(String text) {
    if (text.startsWith(protocol)) {
      return text.substring(protocol.length());
    } else {
      return text;
    }
  }

}
