package edu.wpi.first.shuffleboard.sources;

/**
 * The 'null' data source type. This has no name, no data, and is never active.
 */
class EmptyDataSource<T> extends AbstractDataSource<T> {

  public EmptyDataSource() {
    setName("");
    setData(null);
    setActive(false);
  }

}
