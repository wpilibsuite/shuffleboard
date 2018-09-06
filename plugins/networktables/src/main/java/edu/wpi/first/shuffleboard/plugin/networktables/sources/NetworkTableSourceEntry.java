package edu.wpi.first.shuffleboard.plugin.networktables.sources;


import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceEntry;

import edu.wpi.first.networktables.NetworkTable;

import java.util.Arrays;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A simple value class for information about an entry in NetworkTables.
 */
public final class NetworkTableSourceEntry implements SourceEntry {

  private final String key;
  private final String simpleKey;
  private final Object value;
  private final String displayString;

  /**
   * Creates a new network table source entry.
   *
   * @param key   the network table key the source would be for
   * @param value the value that the source would contain
   */
  public NetworkTableSourceEntry(String key, Object value) {
    this.key = key;
    this.simpleKey = NetworkTable.basenameKey(key);
    this.value = value;
    this.displayString = displayStringForValue(value);
  }

  private static String displayStringForValue(Object value) {
    if (value == null) {
      return "";
    }
    if (value instanceof double[]) {
      return Arrays.toString((double[]) value);
    }
    if (value instanceof String[]) {
      return Arrays.toString((String[]) value);
    }
    if (value instanceof boolean[]) {
      return Arrays.toString((boolean[]) value);
    }
    if (value instanceof byte[]) {
      StringJoiner joiner = new StringJoiner(", ", "[", "]");
      for (byte raw : (byte[]) value) {
        // Display as unsigned hexadecimal strings, eg 15 -> 0x0F
        joiner.add(String.format("0x%02X", 0xFF & raw));
      }
      return joiner.toString();
    }
    return value.toString();
  }

  @Override
  public String getName() {
    return getKey();
  }

  @Override
  public Object getValue() {
    return value;
  }

  public String getKey() {
    return key;
  }

  public String simpleKey() {
    return simpleKey;
  }

  @Override
  public String getViewName() {
    return simpleKey;
  }

  @Override
  public String getValueView() {
    return displayString;
  }

  public String getDisplayString() {
    return displayString;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }

    NetworkTableSourceEntry that = (NetworkTableSourceEntry) obj;

    return Objects.equals(key, that.key) && Objects.equals(displayString, that.displayString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, displayString);
  }

  @Override
  public String toString() {
    return String.format("NetworkTableSourceEntry(key='%s', displayString='%s')", key, displayString);
  }

  @Override
  public DataSource get() {
    return NetworkTableSource.forKey(getKey());
  }
}
