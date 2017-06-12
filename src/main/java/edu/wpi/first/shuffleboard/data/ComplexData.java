package edu.wpi.first.shuffleboard.data;

import edu.wpi.first.shuffleboard.widget.DataType;

import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

/**
 * A complex data type backed internally by an observable map. Subtypes should have properties
 * bound to specific keys in the map.
 */
public class ComplexData {

  protected final ObservableMap<String, Object> map;

  protected ComplexData(ObservableMap<String, Object> map) {
    this.map = map;
  }

  public ObservableMap<String, Object> getMap() {
    return FXCollections.unmodifiableObservableMap(map);
  }

  /**
   * Creates a new complex data object for the given data type and backed by the given map.
   *
   * @param dataType the data type to create the complex data object for
   * @param map      the map that the data should be backed by
   */
  public static ComplexData forDataType(DataType dataType, ObservableMap<String, Object> map) {
    switch (dataType) {
      case Map:
        return new ComplexData(map);
      case SendableChooser:
        return new SendableChooserData(map);
      default:
        throw new UnsupportedOperationException(
            "Cannot create complex data for type: " + dataType.getName());
    }
  }

}
