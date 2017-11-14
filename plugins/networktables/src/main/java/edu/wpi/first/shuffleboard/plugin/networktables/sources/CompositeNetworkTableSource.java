package edu.wpi.first.shuffleboard.plugin.networktables.sources;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.shuffleboard.api.data.ComplexData;
import edu.wpi.first.shuffleboard.api.data.ComplexDataType;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A network table source for composite data, ie data stored in multiple key-value pairs or nested
 * tables. The data is represented as a single map of keys (which may be multi-level ie "foo" or
 * "a/b" or "a/b/c/.../") to their values in the tables. This takes advantage of the fact that
 * network tables is a flat namespace and that a subtable is really just a shortcut for finding data
 * under a certain nested namespace.
 */
public class CompositeNetworkTableSource<D extends ComplexData<D>> extends NetworkTableSource<D> {

  private final Map<String, Object> backingMap = new HashMap<>();
  private final ComplexDataType<D> dataType;

  /**
   * Creates a composite network table source backed by the values associated with the given
   * subtable name.
   *
   * @param tableName the full path of the subtable backing this source
   * @param dataType  the data type for this source to accept
   */
  @SuppressWarnings("PMD.ConstructorCallsOverridableMethod") // PMD is dumb
  public CompositeNetworkTableSource(String tableName, ComplexDataType<D> dataType) {
    super(tableName, dataType);
    this.dataType = dataType;
    String path = NetworkTableUtils.normalizeKey(tableName, false);
    NetworkTable table = NetworkTableInstance.getDefault().getTable(path);
    setData(dataType.getDefaultValue());

    setTableListener((key, value, flags) -> {
      boolean delete = NetworkTableUtils.isDelete(flags);
      String relativeKey = NetworkTableUtils.normalizeKey(key.substring(path.length() + 1), false);
      if (delete) {
        backingMap.remove(relativeKey);
      } else {
        backingMap.put(relativeKey, value);
      }
      Optional<DataType> type = NetworkTableUtils.dataTypeForEntry(fullTableKey);
      setActive(type.map(t -> Objects.equals(t, dataType)).orElse(false));
      setData(dataType.fromMap(backingMap));
    });

    data.addListener((__, oldData, newData) -> {
      if (isUpdateFromNetworkTables()) {
        // The change was from a network change, no need to send it again
        return;
      }
      Map<String, Object> diff = newData.changesFrom(oldData);
      backingMap.putAll(diff);
      if (isConnected()) {
        for (Map.Entry<String, Object> elem : diff.entrySet()) {
          NetworkTableUtils.setEntryValue(table.getEntry(elem.getKey()), elem.getValue());
        }
      }
    });

    Sources.getDefault().register(this);
  }

  @Override
  public ComplexDataType<D> getDataType() {
    return dataType;
  }

}
