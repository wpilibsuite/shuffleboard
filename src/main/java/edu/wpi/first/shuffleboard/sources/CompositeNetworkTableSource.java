package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.widget.DataType;
import edu.wpi.first.wpilibj.networktables.NetworkTablesJNI;
import edu.wpi.first.wpilibj.tables.ITable;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableMap;

/**
 * A network table source for composite data, ie data stored in multiple key-value pairs or nested tables. The data
 * is represented as a single map of keys (which may be multi-level ie "foo" or "a/b" or "a/b/c/.../") to their values
 * in the tables. This takes advantage of the fact that network tables is a flat namespace and that a subtable
 * is really just a shortcut for finding data under a certain nested namespace.
 */
public class CompositeNetworkTableSource extends AbstractDataSource<ObservableMap<String, Object>> {

  /**
   * Creates a composite network table source provided by the given table and with the given data type.
   *
   * @param root     the root table providing all the data for the source
   * @param dataType the type of data being provided
   */
  public CompositeNetworkTableSource(ITable root, DataType dataType) {
    String path = root.toString().substring("Networktable: ".length());
    setName(path.substring(1)); // remove leading "/"
    // Use a synchronized map because network table listeners run in their own thread
    super.setData(FXCollections.synchronizedObservableMap(FXCollections.observableHashMap()));
    NetworkTablesJNI.addEntryListener(
        path,
        (uid, key, value, flags) -> {
          if (!key.startsWith(path)) {
            // Not for us, ignore it
            return;
          }
          boolean delete = (flags & ITable.NOTIFY_DELETE) != 0;
          // 'key' is the full path... shorten it to be relative to the root table we're using
          String shortKey = key.substring(path.length() + 1);
          if (delete) {
            getData().remove(shortKey);
          } else {
            getData().put(shortKey, value);
          }
          if (shortKey.equals("~METADATA~/Type")) {
            if (!dataType.getName().equals(value)) {
              setActive(false);
            } else {
              setActive(!delete);
            }
          }
        },
        ITable.NOTIFY_IMMEDIATE | ITable.NOTIFY_LOCAL | ITable.NOTIFY_NEW | ITable.NOTIFY_DELETE | ITable.NOTIFY_UPDATE);
    getData().addListener((MapChangeListener<String, Object>) change -> {
      if (!isActive()) {
        return;
      }
      if (change.wasAdded()) {
        root.putValue(change.getKey(), change.getValueAdded());
      }
      if (change.wasRemoved() && !change.getMap().containsKey(change.getKey())) {
        root.delete(change.getKey());
      }
    });
  }

  /**
   * Do not use this method.
   */
  @Override
  public void setData(ObservableMap<String, Object> newValue) {
    throw new UnsupportedOperationException("The data cannot be set directly. Set a value using getData() instead");
  }

}
