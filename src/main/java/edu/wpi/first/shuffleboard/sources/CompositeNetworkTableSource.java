package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.util.AsyncUtils;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.wpilibj.tables.ITable;

import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

/**
 * A network table source for composite data, ie data stored in multiple key-value pairs or nested
 * tables. The data is represented as a single map of keys (which may be multi-level ie "foo" or
 * "a/b" or "a/b/c/.../") to their values in the tables. This takes advantage of the fact that
 * network tables is a flat namespace and that a subtable is really just a shortcut for finding data
 * under a certain nested namespace.
 */
public class CompositeNetworkTableSource extends NetworkTableSource<ObservableMap<String, Object>> {

  /**
   * Creates a composite network table source backed by the values associated with the given
   * subtable name.
   *
   * @param tableName the full path of the subtable backing this source
   * @param dataType  the data type for this source to accept
   */
  @SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // PMD is dumb
  public CompositeNetworkTableSource(String tableName, DataType dataType) {
    super(tableName);
    String path = NetworkTableUtils.normalizeKey(tableName, false);
    ITable table = NetworkTableUtils.rootTable.getSubTable(path);
    // Use a synchronized map because network table listeners run in their own thread
    super.setData(FXCollections.synchronizedObservableMap(FXCollections.observableHashMap()));

    setTableListener((key, value, flags) -> {
      AsyncUtils.runAsync(() -> {
        // make sure the updates run on the application thread
        boolean delete = NetworkTableUtils.isDelete(flags);
        String simpleKey = NetworkTableUtils.simpleKey(key);
        if (delete) {
          getData().remove(simpleKey);
        } else {
          getData().put(simpleKey, value);
        }
        setActive(NetworkTableUtils.dataTypeForEntry(fullTableKey) == dataType);
      });
    });

    getData().addListener((MapChangeListener<String, Object>) change -> {
      if (!isActive()) {
        return;
      }
      if (change.wasAdded()) {
        table.putValue(change.getKey(), change.getValueAdded());
      }
      if (change.wasRemoved() && !change.getMap().containsKey(change.getKey())) {
        table.delete(change.getKey());
      }
    });
  }

  /**
   * Do not use this method.
   */
  @Override
  public void setData(ObservableMap<String, Object> newValue) {
    throw new UnsupportedOperationException(
        "The data cannot be set directly. Set a value using getData() instead");
  }

}
