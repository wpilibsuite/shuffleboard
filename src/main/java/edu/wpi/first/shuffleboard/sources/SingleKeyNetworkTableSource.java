package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.wpilibj.tables.ITable;

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
  public SingleKeyNetworkTableSource(ITable table, String key, DataType dataType) {
    super(key);
    setName(key);
    setTableListener((__, value, flags) -> {
      boolean deleted = NetworkTableUtils.isDelete(flags);
      setActive(!deleted && dataType == DataType.valueOf(value.getClass()));

      if (isActive()) {
        setData((T) value);
      } else {
        setData(null);
      }
    });

    data.addListener((__, oldValue, newValue) -> {
      if (isActive()) {
        table.putValue(key, newValue);
      } else {
        setData(oldValue);
      }
    });
  }

}
