package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.wpilibj.tables.ITable;

/**
 * A data source backed by a single key-value pair in a network table.
 */
public class SingleKeyNetworkTableSource<T> extends AbstractDataSource<T> {

  /**
   * Creates a single-key network table source backed by the value in the given table
   * associated with the given key.
   *
   * @param table     the table backing the source
   * @param key       the key associated with the data
   * @param dataTypes the allowable data types. A value that is not an instance of one of these
   *                  types is considered "null" and will make the source inactive
   */
  public SingleKeyNetworkTableSource(ITable table, String key, Class<?>... dataTypes) {
    setName(key);
    table.addTableListenerEx(key, (source, k, v, isNew) -> {
      boolean correctType = false;
      for (Class<?> type : dataTypes) {
        if (type.isInstance(v)) {
          correctType = true;
          break;
        }
      }
      active.setValue(correctType);

      if (isActive()) {
        setData(isActive() ? (T) v : null);
      }
    }, ITable.NOTIFY_IMMEDIATE | ITable.NOTIFY_LOCAL | ITable.NOTIFY_NEW | ITable.NOTIFY_UPDATE);

    table.addTableListenerEx(key, (s, k, v, n) -> active.setValue(false), ITable.NOTIFY_DELETE);

    data.addListener((obs, prev, cur) -> {
      if (isActive()) {
        table.putValue(key, cur);
      }
    });
  }

}
