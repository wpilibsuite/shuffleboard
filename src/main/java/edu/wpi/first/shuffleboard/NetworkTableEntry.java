package edu.wpi.first.shuffleboard;


/**
 * A simple data class for information about an entry in NetworkTables.
 */
public final class NetworkTableEntry {

  private String key;
  private String value;

  public NetworkTableEntry(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public NetworkTableEntry() {
    this("", "");
  }

  public String getKey() {
    return key;
  }

  public String getValue() {
    return value;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(String value) {
    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    NetworkTableEntry that = (NetworkTableEntry) o;

    if (key != null ? !key.equals(that.key) : that.key != null) return false;
    return value != null ? value.equals(that.value) : that.value == null;
  }

  @Override
  public int hashCode() {
    int result = key != null ? key.hashCode() : 0;
    result = 31 * result + (value != null ? value.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "NetworkTableEntry(" +
        "key='" + key + '\'' +
        ", value='" + value + '\'' +
        ')';
  }
}
