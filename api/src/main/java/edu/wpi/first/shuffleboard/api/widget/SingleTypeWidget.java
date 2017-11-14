package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;

import com.google.common.collect.Iterables;

import java.util.Set;

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

  /**
   * Gets the single data type for this widget.
   *
   * @throws IllegalStateException if there isn't exactly one data type defined for this widget
   */
  @SuppressWarnings("unchecked")
  default DataType<T> getDataType() throws IllegalStateException {
    Set<DataType> dataTypes = getDataTypes();
    if (dataTypes.isEmpty()) {
      throw new IllegalStateException(
          String.format(
              "No data types defined for %s! Make sure the required data types are exported by the defining plugin",
              getClass().getName()
          )
      );
    } else if (dataTypes.size() > 1) {
      throw new IllegalStateException("Multiple data types are defined for a SingleTypeWidget! " + dataTypes);
    }
    return Iterables.get(dataTypes, 0);
  }

  /**
   * Sets the current value of the data source.
   */
  default void setData(T data) {
    dataProperty().setValue(data);
  }

}
