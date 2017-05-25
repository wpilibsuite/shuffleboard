package edu.wpi.first.shuffleboard.dnd;

import edu.wpi.first.shuffleboard.sources.DataSource;
import edu.wpi.first.shuffleboard.sources.NetworkTableSource;
import edu.wpi.first.shuffleboard.util.NetworkTableUtils;

import java.io.Serializable;

/**
 * A class used to handle dragging of data sources into the main pane.
 */
public class DataSourceTransferable implements Serializable {

  public final String name;
  public final DataSourceFactory sourceFactory;

  private DataSourceTransferable(String name, DataSourceFactory sourceFactory) {
    this.name = name;
    this.sourceFactory = sourceFactory;
  }

  /**
   * Creates a source from this transferable.
   */
  public DataSource<?> createSource() {
    return sourceFactory.create(name);
  }

  /**
   * Creates a transferable network table source.
   *
   * @param key the full key in network tables that the created source should be associated with.
   */
  public static DataSourceTransferable networkTable(String key) {
    return new DataSourceTransferable(
        NetworkTableUtils.normalizeKey(key),
        new NetworkTableDataSourceFactory()
    );
  }

  private interface DataSourceFactory extends Serializable {
    DataSource<?> create(String name);
  }

  private static class NetworkTableDataSourceFactory implements DataSourceFactory {
    @Override
    public DataSource create(String name) {
      return NetworkTableSource.forKey(name);
    }
  }

}
