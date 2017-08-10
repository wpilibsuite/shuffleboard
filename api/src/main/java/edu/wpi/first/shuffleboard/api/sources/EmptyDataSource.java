package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.data.DataTypes;

/**
 * The 'null' data source type. This has no name, no data, and is never active.
 */
class EmptyDataSource<T> extends AbstractDataSource<T> {

  public EmptyDataSource() {
    super(DataTypes.None);
    setName("");
    setData(null);
    setActive(false);
  }

  @Override
  public SourceType getType() {
    return SourceTypes.Static;
  }

}
