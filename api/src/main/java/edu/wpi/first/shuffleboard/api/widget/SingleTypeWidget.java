package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;

import javafx.beans.property.Property;

public interface SingleTypeWidget<T> extends Widget {

  /**
   * Gets a property containing the value of the source. This is used for brevity and is a shortcut for
   * {@code EasyBind.monadic(sourceProperty()).selectProperty(DataSource::dataProperty)}
   */
  Property<T> dataProperty();

  /**
   * Gets the current value of the data source.
   */
  default T getData() {
    return dataProperty().getValue();
  }

  default DataType<T> getDataType() {
    return getDataTypes().toArray(new DataType[1])[0];
  }

  /**
   * Sets the current value of the data source.
   */
  default void setData(T data) {
    dataProperty().setValue(data);
  }

}
