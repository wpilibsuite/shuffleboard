package edu.wpi.first.shuffleboard.app;


import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.app.sources.NetworkTableSource;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 * A simple value class for information about an entry in NetworkTables.
 */
public final class NetworkTableEntry implements SourceEntry {

  private final String key;
  private final String displayString;

  public NetworkTableEntry(String key, String displayString) {
    this.key = key;
    this.displayString = displayString;
  }

  public NetworkTableEntry(String key, Object value) {
    this(key, displayStringForValue(value));
  }

  public NetworkTableEntry() {
    this("", "");
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
    return value.toString();
  }

  @Override
  public String getName() {
    return getKey();
  }

  @Override
  public Class<? extends DataSource> getType() {
    return NetworkTableSource.class;
  }

  public String getKey() {
    return key;
  }

  public String simpleKey() {
    return NetworkTableUtils.simpleKey(key);
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

    NetworkTableEntry that = (NetworkTableEntry) obj;

    return Objects.equals(key, that.key) && Objects.equals(displayString, that.displayString);
  }

  @Override
  public int hashCode() {
    return Objects.hash(key, displayString);
  }

  @Override
  public String toString() {
    return String.format("NetworkTableEntry(key='%s', displayString='%s')", key, displayString);
  }

  @Override
  public DataSource get() {
    return NetworkTableSource.forKey(getKey());
  }
}
