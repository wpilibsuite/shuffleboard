package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.AbstractDataSource;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.util.AsyncUtils;
import edu.wpi.first.shuffleboard.plugin.networktables.util.NetworkTableUtils;
import edu.wpi.first.networktables.GenericSubscriber;
import edu.wpi.first.networktables.MultiSubscriber;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.PubSubOption;

import java.util.Arrays;
import java.util.Comparator;
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
  private MultiSubscriber multiSub;
  private GenericSubscriber singleSub;
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
    if (listenerUid != -1) {
      inst.removeListener(listenerUid);
    }
    if (multiSub != null) {
      multiSub.close();
    }
    if (singleSub != null) {
      singleSub.close();
    }
    setConnected(true);
    if (isSingular()) {
      // Handle leading slashes. Topic names are exact and do no normalization
      String topicName;
      if (Arrays.stream(inst.getTopicInfo()).anyMatch(t -> t.name.equals(fullTableKey))) {
        topicName = fullTableKey;
      } else {
        if (fullTableKey.startsWith("/")) {
          topicName = NetworkTable.normalizeKey(fullTableKey, false);
        } else {
          topicName = NetworkTable.normalizeKey(fullTableKey, true);
        }
      }

      singleSub = inst.getTopic(topicName).genericSubscribe(PubSubOption.hidden(false), PubSubOption.sendAll(true));
      listenerUid = inst.addListener(
        singleSub,
        EnumSet.of(
          NetworkTableEvent.Kind.kImmediate,
          NetworkTableEvent.Kind.kTopic,
          NetworkTableEvent.Kind.kValueAll),
        event -> {
          if (isConnected()) {
            AsyncUtils.runAsync(() -> {
              try {
                ntUpdate = true;
                listener.onChange(fullTableKey, event);
              } finally {
                ntUpdate = false;
              }
            });
          }
        });
    } else {
      multiSub = new MultiSubscriber(
          inst,
          new String[] {fullTableKey},
          PubSubOption.hidden(false),
          PubSubOption.sendAll(true)
      );
      listenerUid = inst.addListener(
        multiSub,
        EnumSet.of(
          NetworkTableEvent.Kind.kImmediate,
          NetworkTableEvent.Kind.kTopic,
          NetworkTableEvent.Kind.kValueAll),
        event -> {
          String name = NetworkTableUtils.topicNameForEvent(event);
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
    if (multiSub != null) {
      multiSub.close();
    }
    if (singleSub != null) {
      singleSub.close();
    }
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
    String key = fullTableKey;
    final String uri = NetworkTableSourceType.getInstance().toUri(key);
    return sources.computeIfAbsent(uri, __ -> {
      DataType<?> lookup = NetworkTableUtils.dataTypeForEntry(key);
      if (lookup == DataTypes.Unknown) {
        // No known data type, fall back to generic map data
        lookup = DataTypes.Map;
      }
      if (lookup.isComplex()) {
        return new CompositeNetworkTableSource<>(key, (ComplexDataType<?>) lookup);
      } else {
        return new SingleKeyNetworkTableSource<>(NetworkTableUtils.rootTable, key,
                NetworkTableUtils.dataTypeForEntry(key));
      }
    });
  }

  /**
   * Gets the existing data source for the given key, or {@link Optional#empty()} is no source has been created for that
   * key.
   */
  public static Optional<NetworkTableSource> getExisting(String key) {
    return Optional.ofNullable(sources.get(key));
  }


}
