package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.widget.DataType;

/**
 * The 'null' data source type. This has no name, no data, and is never active.
 */
class EmptyDataSource<T> extends AbstractDataSource<T> {

  public EmptyDataSource() {
    super(DataType.Unknown);
    setName("");
    setData(null);
    setActive(false);
  }

  @Override
  public Type getType() {
    return Type.NONE;
  }

}
