package edu.wpi.first.shuffleboard;


import java.util.Objects;

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
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    NetworkTableEntry that = (NetworkTableEntry) o;

    return Objects.equals(key, that.key) && Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, value);
  }

  @Override
  public String toString() {
    return String.format("NetworkTableEntry(key='%s', value='%s')", key, value);
  }
}
