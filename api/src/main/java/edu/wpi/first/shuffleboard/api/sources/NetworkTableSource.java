package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;

/**
 * A source for data in network tables. Data can be a single value or a map of keys to values.
 *
 * @implNote Subclasses must call {@link #setTableListener(TableListener) setTableListener()} after
 *           the super constructor call. If a subclass needs to implement {@link #close()}, its
 *           implementation <i>must</i> call {@code super.close()} to properly remove the listener
 *           from network tables.
 */
public abstract class NetworkTableSource<T> extends AbstractDataSource<T> {

  protected final String fullTableKey;
  private int listenerUid = -1;
  private volatile boolean ntUpdate = false;

  /**
   * Creates a network table source that listens to values under the given key. The key can be
   * a path to a single key-value pair (for single key sources) or a subtable
   * (for composite sources).
   *
   * @param fullTableKey the full path
   */
  protected NetworkTableSource(String fullTableKey) {
    super(NetworkTableUtils.dataTypeForEntry(fullTableKey));
    this.fullTableKey = NetworkTableUtils.normalizeKey(fullTableKey, true);
    setName(fullTableKey);
  }

  /**
   * Sets the table listener to call when a value changes under this source's key.
   */
  protected final void setTableListener(TableListener listener) {
    NetworkTablesJNI.removeEntryListener(listenerUid);
    listenerUid = NetworkTablesJNI.addEntryListener(
        fullTableKey,
        (uid, key, value, flags) -> {
          if (isConnected()) {
            AsyncUtils.runAsync(() -> {
              ntUpdate = true;
              listener.onChange(key, value, flags);
              ntUpdate = false;
            });
          }
        },
        0xFF);
    connect();
  }

  protected boolean isUpdateFromNetworkTables() {
    return ntUpdate;
  }

  public String getKey() {
    return fullTableKey;
  }

  @Override
  public String getId() {
    return getType().toUri(fullTableKey);
  }

  @Override
  public SourceType getType() {
    return SourceType.NETWORK_TABLE;
  }

  @Override
  public void close() {
    NetworkTablesJNI.removeEntryListener(listenerUid);
  }

  @FunctionalInterface
  protected interface TableListener {

    /**
     * Called when a value changes in network tables.
     *
     * @param key   the key associated with the value that changed
     * @param value the new value. This will <i>never</i> be null.
     * @param flags the network table flags for the change
     */
    void onChange(String key, Object value, int flags);

  }

  /**
   * Creates a data source for the given network table key.
   *
   * @param fullTableKey the full key in network tables eg "/foo/bar"
   *
   * @return a data source for that key, or {@link DataSource#none()} if that key does not exist
   */
  @SuppressWarnings("unchecked")
  public static DataSource<?> forKey(String fullTableKey) {
    String key = NetworkTableUtils.normalizeKey(fullTableKey, false);
    final String uri = SourceType.NETWORK_TABLE.toUri(key);
    if (NetworkTableUtils.rootTable.containsKey(key)) {
      // Key-value pair
      return Sources.computeIfAbsent(uri, () ->
          new SingleKeyNetworkTableSource<>(NetworkTableUtils.rootTable, key,
              NetworkTableUtils.dataTypeForEntry(key)));
    }
    if (NetworkTableUtils.rootTable.containsSubTable(key) || key.isEmpty()) {
      // Composite
      return Sources.computeIfAbsent(uri, () ->
          new CompositeNetworkTableSource(key, (ComplexDataType) NetworkTableUtils.dataTypeForEntry(key)));
    }
    return DataSource.none();
  }


}
