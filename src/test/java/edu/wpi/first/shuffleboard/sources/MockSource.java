package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.widget.DataType;

public class MockSource<T> extends AbstractDataSource<T> {

  public MockSource(String name, DataType dataType) {
    super(dataType);
    setName(name);
  }

  @Override
  public void setName(String name) {
    super.setName(name);
  }

}
