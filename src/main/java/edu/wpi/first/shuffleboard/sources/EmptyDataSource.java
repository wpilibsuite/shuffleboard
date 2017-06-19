package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.data.DataTypes;

/**
 * The 'null' data source type. This has no name, no data, and is never active.
 */
class EmptyDataSource<T> extends AbstractDataSource<T> {

  public EmptyDataSource() {
    super(DataTypes.Unknown);
    setName("");
    setData(null);
    setActive(false);
  }

}
