package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.util.EqualityUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;

/**
 * A data source backed by a single key-value pair in a network table.
 */
public class SingleKeyNetworkTableSource<T> extends NetworkTableSource<T> {
  /**
   * Creates a single-key network table source backed by the value in the given table
   * associated with the given key.
   *
   * @param table    the table backing the source
   * @param key      the key associated with the data
   * @param dataType the allowable data type. A value that is not an instance of this
   *                 type is considered "null" and will make the source inactive
   */
  public SingleKeyNetworkTableSource(NetworkTable table, String key, DataType dataType) {
    super(key, dataType);
    setName(key);
    setTableListener((__, value, flags) -> {
      if (EqualityUtils.isEqual(value, getData())) {
        // No change
        return;
      }
      boolean deleted = NetworkTableUtils.isDelete(flags);
      setActive(!deleted && DataTypes.getDefault().forJavaType(value.getClass()).map(dataType::equals).orElse(false));

      if (isActive()) {
        setData((T) value);
      } else {
        setData(null);
      }
    });

    data.addListener((__, oldValue, newValue) -> {
      if (isUpdateFromNetworkTables()) {
        // The change was from network tables; setting the value again would be redundant
        return;
      }

      NetworkTableEntry entry = table.getEntry(key);
      Object value = entry.getValue().getValue();

      if ((value != null && EqualityUtils.isEqual(value, newValue)) || !isConnected()) {
        // no change
        return;
      }

      if (isActive()) {
        NetworkTableUtils.setEntryValue(entry, newValue);
      } else {
        throw new IllegalStateException("Source is not active");
      }
    });

    Sources.getDefault().register(this);
  }
}
