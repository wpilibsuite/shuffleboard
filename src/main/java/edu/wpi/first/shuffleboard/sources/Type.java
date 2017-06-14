package edu.wpi.first.shuffleboard.sources;

public enum Type {

  NONE(false) {
    @Override
    public String toUri(String sourceName) {
      return null;
    }
  },
  NETWORK_TABLE(true) {
    @Override
    public String toUri(String sourceName) {
      return "network_table://" + sourceName;
    }
  },
  CAMERA_SERVER(true) {
    @Override
    public String toUri(String sourceName) {
      return "camera_server://" + sourceName;
    }
  };

  public final boolean isRecordable;

  Type(boolean isRecordable) {
    this.isRecordable = isRecordable;
  }

  public abstract String toUri(String sourceName);

}
