package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.AbstractDataSource;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A source for data in network tables. Data can be a single value or a map of keys to values.
 *
 * @implNote Subclasses must call {@link #setTableListener(TableListener) setTableListener()} after
 *           the super constructor call. If a subclass needs to implement {@link #close()}, its
 *           implementation <i>must</i> call {@code super.close()} to properly remove the listener
 *           from network tables.
 */
public abstract class NetworkTableSource<T> extends AbstractDataSource<T> {

  private static final Map<String, NetworkTableSource> sources = new ConcurrentHashMap<>();

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
    this(fullTableKey, NetworkTableUtils.dataTypeForEntry(fullTableKey));
  }

  protected NetworkTableSource(String fullTableKey, DataType<T> dataType) {
    super(dataType);
    this.fullTableKey = NetworkTable.normalizeKey(fullTableKey, true);
    setName(fullTableKey);
  }

  /**
   * Sets the table listener to call when a value changes under this source's key.
   */
  protected final void setTableListener(TableListener listener) {
    NetworkTableInstance inst = NetworkTableInstance.getDefault();
    inst.removeListener(listenerUid);
    setConnected(true);
    listenerUid = inst.addListener(
        new String[] {fullTableKey},
        EnumSet.of(
          NetworkTableEvent.Kind.kImmediate,
          NetworkTableEvent.Kind.kTopic,
          NetworkTableEvent.Kind.kValueAll),
        event -> {
          String name = NetworkTableUtils.topicNameForEvent(event);
          if (isSingular() && !name.equals(fullTableKey)) {
            // Since NetworkTableInstance.addEntryListener() will fire on anything that starts with the key,
            // a singular source will be notified for an unrelated entry.
            // For example, a singular source for the entry "/S" will also be fired for any changing entry that
            // starts with "/S", such as "/SmartDashboard/<anything>" or "/SomeUnrelatedEntry".
            // This check prevents the source from being erroneously updated for an unrelated entry.
            return;
          }
          if (isConnected()) {
            AsyncUtils.runAsync(() -> {
              try {
                ntUpdate = true;
                listener.onChange(name, event);
              } finally {
                ntUpdate = false;
              }
            });
          }
        });
  }

  /**
   * Checks if this source is singular; i.e. is for a single entry only.
   *
   * @return true if this source is for a single entry, false if not
   */
  protected abstract boolean isSingular();

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
    return NetworkTableSourceType.getInstance();
  }

  @Override
  public void close() {
    setActive(false);
    setConnected(false);
    NetworkTableInstance.getDefault().removeListener(listenerUid);
    Sources.getDefault().unregister(this);
    sources.remove(getId());
  }

  @FunctionalInterface
  protected interface TableListener {

    /**
     * Called when a value changes in network tables.
     *
     * @param key   the key associated with the value that changed
     * @param event the event
     */
    void onChange(String key, NetworkTableEvent event);

  }

  /**
   * Removes a cached NetworkTable source for the given source ID.
   */
  public static void removeCachedSource(String sourceId) {
    sources.remove(sourceId);
  }

  /**
   * Removes all cached NetworkTable sources.
   */
  public static void removeAllCachedSources() {
    sources.clear();
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
    String key = NetworkTable.normalizeKey(fullTableKey, false);
    final String uri = NetworkTableSourceType.getInstance().toUri(key);
    if (NetworkTableUtils.rootTable.containsKey(key)) {
      // Key-value pair
      return sources.computeIfAbsent(uri, __ ->
          new SingleKeyNetworkTableSource<>(NetworkTableUtils.rootTable, key,
              NetworkTableUtils.dataTypeForEntry(key)));
    }
    if (NetworkTableUtils.rootTable.containsSubTable(key) || key.isEmpty()) {
      // Composite
      return sources.computeIfAbsent(uri, __ ->
          new CompositeNetworkTableSource(key, (ComplexDataType) NetworkTableUtils.dataTypeForEntry(key)));
    }
    return DataSource.none();
  }

  /**
   * Gets the existing data source for the given key, or {@link Optional#empty()} is no source has been created for that
   * key.
   */
  public static Optional<NetworkTableSource> getExisting(String key) {
    return Optional.ofNullable(sources.get(key));
  }


}
