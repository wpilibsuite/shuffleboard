package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.util.EqualityUtils;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;
import edu.wpi.first.wpilibj.tables.TableKeyNotDefinedException;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

import static edu.wpi.first.shuffleboard.util.NetworkTableUtils.concat;
import static edu.wpi.first.shuffleboard.util.NetworkTableUtils.normalizeKey;

/**
 * An abstraction layer on top of ntcore that allows us to modify network table sources without affecting the underlying
 * network data.
 *
 * <p>Notable differences from the {@link NetworkTable} implementation:
 * <ul>
 * <li>No support for entry flags</li>
 * <li>No support for persistence</li>
 * <li>No support for any deprecated method, with the exception of {@link #getValue(String)}</li>
 * </ul>
 *
 * <p>Additionally, this class can be connected or disconnected from ntcore at any time with
 * {@link #connectedProperty()} and its associated {@link #isConnected getter} and {@link #setConnected setter}. By
 * default, this is connected.
 */
public final class MapBackedTable implements ITable {

  /**
   * Global map of key-value pairs.
   */
  private static final ObservableMap<String, Object> values
      = FXCollections.synchronizedObservableMap(FXCollections.observableHashMap());

  /**
   * All known tables.
   */
  private static final Map<String, MapBackedTable> tables = new HashMap<>();

  /**
   * All listeners.
   */
  private static final Map<String, Set<ITableListener>> listeners = new HashMap<>();

  private static BooleanProperty connected = new SimpleBooleanProperty(MapBackedTable.class, "connected", true);

  private final String path;

  private MapBackedTable(String path) {
    this.path = normalizeKey(path, true);
  }

  private static final MapBackedTable root = getTable("/");

  /**
   * Gets the root table.
   */
  public static MapBackedTable getRoot() {
    return root;
  }

  /**
   * Gets the table with the given path, creating it if needed.
   *
   * @param key the path of the table to get
   */
  public static MapBackedTable getTable(String key) {
    key = normalizeKey(key, true);
    return tables.computeIfAbsent(key, MapBackedTable::new);
  }

  static {
    // Update the value map when the networked tables changed
    NetworkTablesJNI.addEntryListener("", (uid, key, value, flags) -> {
      if (isConnected()) {
        key = normalizeKey(key);
        if (NetworkTableUtils.isDelete(flags)) {
          values.remove(key);
        } else if (EqualityUtils.isDifferent(value, values.get(key))) {
          // Create parent tables, if they don't already exist
          final List<String> hierarchy = NetworkTableUtils.getHierarchy(key);
          hierarchy.stream()
              .limit(hierarchy.size() - 1)
              .forEach(MapBackedTable::getTable);
          values.put(key, value);
        }
      }
    }, 0xFF);

    values.addListener((MapChangeListener<String, Object>) change -> {
      final String changedKey = change.getKey();
      if (change.wasAdded() && !change.wasRemoved()) {
        // new
        listeners.forEach((key, listenerSet) -> {
          if ((tables.containsKey(key) && changedKey.startsWith(key)) || (key.equals(changedKey))) {
            listenerSet.forEach(listener -> {
              listener.valueChangedEx(root, changedKey, change.getValueAdded(), NOTIFY_NEW | NOTIFY_LOCAL);
            });
          }
        });
        if (isConnected() && !onNtListenerThread()) {
          NetworkTable.getTable("/").putValue(changedKey, change.getValueAdded());
        }
      } else if (change.wasRemoved() && change.getMap().containsKey(changedKey)) {
        // updated
        listeners.forEach((key, listenerSet) -> {
          if ((tables.containsKey(key) && changedKey.startsWith(key)) || (key.equals(changedKey))) {
            listenerSet.forEach(listener -> {
              listener.valueChangedEx(root, changedKey, change.getValueAdded(), NOTIFY_UPDATE | NOTIFY_LOCAL);
            });
          }
        });
        if (isConnected() && !onNtListenerThread()) {
          NetworkTable.getTable("/").putValue(changedKey, change.getValueAdded());
        }
      } else if (change.wasRemoved()) {
        // removed
        listeners.forEach((key, listenerSet) -> {
          if ((tables.containsKey(key) && changedKey.startsWith(key)) || (key.equals(changedKey))) {
            listenerSet.forEach(listener -> {
              listener.valueChangedEx(root, changedKey, change.getValueRemoved(), NOTIFY_DELETE | NOTIFY_LOCAL);
            });
          }
        });
        if (isConnected() && !onNtListenerThread()) {
          NetworkTable.getTable("/").delete(changedKey);
        }
      }
    });
  }

  private static boolean onNtListenerThread() {
    return Thread.currentThread().getName().equals("NTListener");
  }

  /**
   * Checks if there is a connection to ntcore.
   */
  public static boolean isConnected() {
    return connected.get();
  }

  public static BooleanProperty connectedProperty() {
    return connected;
  }

  /**
   * Connects or disconnects to ntcore.
   *
   * @param connected true if there should be a connection, false if not
   */
  public static void setConnected(boolean connected) {
    MapBackedTable.connected.set(connected);
  }

  public String toAbsoluteKey(String key) {
    return concat(path, key);
  }

  /**
   * Clears the table.
   */
  public void clear() {
    getKeys().forEach(this::delete);
  }

  @Override
  public boolean containsKey(String key) {
    return values.containsKey(toAbsoluteKey(key));
  }

  @Override
  public boolean containsSubTable(String key) {
    return values.keySet()
        .stream()
        .filter(p -> p.startsWith(path))
        .filter(p -> p.length() > path.length())
        .map(p -> p.substring(path.length()))
        .filter(p -> p.contains("/"))
        .count() > 0;
  }

  @Override
  public ITable getSubTable(String key) {
    return getTable(toAbsoluteKey(key));
  }

  /**
   * {@inheritDoc}.
   *
   * @param types ignored; all keys are returned
   */
  @Override
  public Set<String> getKeys(int types) {
    return getKeys();
  }

  @Override
  public Set<String> getKeys() {
    return values.keySet()
        .stream()
        .filter(p -> p.startsWith(path))
        .collect(Collectors.toSet());
  }

  @Override
  public Set<String> getSubTables() {
    return null;
  }

  @Override
  public void setPersistent(String key) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public void clearPersistent(String key) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public boolean isPersistent(String key) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public void setFlags(String key, int flags) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public void clearFlags(String key, int flags) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public int getFlags(String key) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public void delete(String key) {
    values.remove(toAbsoluteKey(key));
  }

  private boolean isCorrectType(String key, Class<?> type, Class<?>... otherTypes) {
    if (containsKey(key)) {
      final Class<?> current = getValue(key).getClass();
      if (current.isAssignableFrom(type)) {
        return true;
      }
      for (Class<?> c : otherTypes) {
        if (current.isAssignableFrom(c)) {
          return true;
        }
      }
      return false;
    } else {
      return true;
    }
  }

  @Override
  public Object getValue(String key) throws TableKeyNotDefinedException {
    if (!values.containsKey(toAbsoluteKey(key))) {
      throw new TableKeyNotDefinedException("No entry for key: " + key + " in table: " + path);
    }
    return values.get(toAbsoluteKey(key));
  }

  @Override
  public Object getValue(String key, Object defaultValue) {
    return values.getOrDefault(toAbsoluteKey(key), defaultValue);
  }

  @Override
  public boolean putValue(String key, Object value) throws IllegalArgumentException {
    if (isCorrectType(key, value.getClass())) {
      values.put(toAbsoluteKey(key), value);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public void retrieveValue(String key, Object externalValue) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public boolean putNumber(String key, double value) {
    if (isCorrectType(key, double.class)) {
      values.put(toAbsoluteKey(key), value);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean setDefaultNumber(String key, double defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public double getNumber(String key) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  public double getNumber(String key, double defaultValue) {
    return (double) values.getOrDefault(toAbsoluteKey(key), defaultValue);
  }

  @Override
  public boolean putString(String key, String value) {
    if (isCorrectType(key, String.class)) {
      values.put(toAbsoluteKey(key), value);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean setDefaultString(String key, String defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public String getString(String key) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  public String getString(String key, String defaultValue) {
    return (String) values.getOrDefault(toAbsoluteKey(key), defaultValue);
  }

  @Override
  public boolean putBoolean(String key, boolean value) {
    if (isCorrectType(key, boolean.class)) {
      values.put(toAbsoluteKey(key), value);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean setDefaultBoolean(String key, boolean defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public boolean getBoolean(String key) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  public boolean getBoolean(String key, boolean defaultValue) {
    return (boolean) values.getOrDefault(toAbsoluteKey(key), defaultValue);
  }

  @Override
  public boolean putBooleanArray(String key, boolean[] value) {
    if (isCorrectType(key, boolean[].class)) {
      values.put(toAbsoluteKey(key), value);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean putBooleanArray(String key, Boolean[] value) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public boolean setDefaultBooleanArray(String key, boolean[] defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public boolean setDefaultBooleanArray(String key, Boolean[] defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public boolean[] getBooleanArray(String key) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  public boolean[] getBooleanArray(String key, boolean[] defaultValue) {
    return (boolean[]) values.getOrDefault(toAbsoluteKey(key), defaultValue);
  }

  @Override
  public Boolean[] getBooleanArray(String key, Boolean[] defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public boolean putNumberArray(String key, double[] value) {
    if (isCorrectType(key, double[].class)) {
      values.put(toAbsoluteKey(key), value);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean putNumberArray(String key, Double[] value) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public boolean setDefaultNumberArray(String key, double[] defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public boolean setDefaultNumberArray(String key, Double[] defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public double[] getNumberArray(String key) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  public double[] getNumberArray(String key, double[] defaultValue) {
    return (double[]) values.getOrDefault(key, defaultValue);
  }

  @Override
  public Double[] getNumberArray(String key, Double[] defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public boolean putStringArray(String key, String[] value) {
    if (isCorrectType(key, String[].class)) {
      values.put(key, value);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean setDefaultStringArray(String key, String[] defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public String[] getStringArray(String key) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  public String[] getStringArray(String key, String[] defaultValue) {
    return (String[]) values.getOrDefault(toAbsoluteKey(key), defaultValue);
  }

  @Override
  public boolean putRaw(String key, byte[] value) {
    if (isCorrectType(key, byte[].class)) {
      values.put(toAbsoluteKey(key), value);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean putRaw(String key, ByteBuffer value, int len) {
    if (!value.isDirect()) {
      throw new IllegalArgumentException("Must be a direct buffer");
    }
    if (value.capacity() < len) {
      throw new IllegalArgumentException("Buffer is too small, must be at least " + len);
    }
    return putRaw(toAbsoluteKey(key), Arrays.copyOfRange(value.array(), 0, len));
  }

  @Override
  public boolean setDefaultRaw(String key, byte[] defaultValue) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public byte[] getRaw(String key) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  public byte[] getRaw(String key, byte[] defaultValue) {
    return (byte[]) values.getOrDefault(toAbsoluteKey(key), defaultValue);
  }

  @Override
  public void addTableListener(ITableListener listener) {
    addTableListenerEx(listener, NOTIFY_NEW | NOTIFY_UPDATE);
  }

  @Override
  public void addTableListener(ITableListener listener, boolean immediateNotify) {
    int flags = NOTIFY_NEW | NOTIFY_UPDATE;
    if (immediateNotify) {
      flags |= NOTIFY_IMMEDIATE;
    }
    addTableListenerEx(listener, flags);
  }

  @Override
  public void addTableListener(String key, ITableListener listener, boolean immediateNotify) {
    int flags = NOTIFY_NEW | NOTIFY_UPDATE;
    if (immediateNotify) {
      flags |= NOTIFY_IMMEDIATE;
    }
    addTableListenerEx(key, listener, flags);
  }

  @Override
  public void addTableListenerEx(ITableListener listener, int flags) {
    listeners.computeIfAbsent(path, __ -> new HashSet<>()).add(listener);
    if ((flags & NOTIFY_IMMEDIATE) != 0) {
      getKeys().forEach(key -> {
        listener.valueChangedEx(this, key, getValue(key), NOTIFY_UPDATE);
      });
    }
  }

  @Override
  public void addTableListenerEx(String key, ITableListener listener, int flags) {
    listeners.computeIfAbsent(toAbsoluteKey(key), __ -> new HashSet<>()).add(listener);
    if ((flags & NOTIFY_IMMEDIATE) != 0) {
      values.forEach((k1, value) -> {
        if (k1.startsWith(toAbsoluteKey(key))) {
          listener.valueChangedEx(this, k1, value, NOTIFY_UPDATE);
        }
      });
    }
  }

  @Override
  public void addSubTableListener(ITableListener listener) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public void addSubTableListener(ITableListener listener, boolean localNotify) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  public void removeTableListener(ITableListener listener) {
    throw new UnsupportedOperationException("Not supported by the abstraction layer");
  }

  @Override
  @Deprecated
  public boolean putInt(String key, int value) {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  @Deprecated
  public int getInt(String key) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  @Deprecated
  public int getInt(String key, int defaultValue) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  public boolean putDouble(String key, double value) {
    if (isCorrectType(key, double.class)) {
      values.put(key, value);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public double getDouble(String key) throws TableKeyNotDefinedException {
    throw new UnsupportedOperationException("deprecated");
  }

  @Override
  public double getDouble(String key, double defaultValue) {
    return (double) values.getOrDefault(toAbsoluteKey(key), defaultValue);
  }

  @Override
  public String getPath() {
    return path;
  }

  @Override
  public String toString() {
    return "MapBackedTable: " + path;
  }

}
