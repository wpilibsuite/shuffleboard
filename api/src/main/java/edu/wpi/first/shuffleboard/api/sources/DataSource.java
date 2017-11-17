package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.data.DataType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

/**
 * A data source provides some kind of data that widgets can display and manipulate. It can be
 * active or inactive; active sources may update at any time, while inactive sources are guaranteed
 * to never update. This can be useful if a widget wants to disable user control while the source
 * can't handle it.
 *
 * @param <T> the type of data provided
 */
public interface DataSource<T> {

  /**
   * Creates a data source with no name, no data, and is never active.
   * This should be used in place of {@code null}.
   *
   * @param <T> the type of the data in the source
   */
  static <T> DataSource<T> none() {
    return new EmptyDataSource<>();
  }

  /**
   * Checks if this data source is active, i.e. its value may update at any time.
   *
   * @return true if this data source is active, false if not
   */
  BooleanProperty activeProperty();

  default boolean isActive() {
    return activeProperty().getValue();
  }

  StringProperty nameProperty();

  /**
   * Gets the name of this data source. This is typically a unique identifier for the data
   * backed by this source.
   */
  default String getName() {
    return nameProperty().getValue();
  }

  Property<T> dataProperty();

  /**
   * Gets the current value of this data source. May return {@code null}
   * if this source isn't active, but may also just return the most recent value.
   */
  default T getData() {
    return dataProperty().getValue();
  }

  default void setData(T newValue) {
    dataProperty().setValue(newValue);
  }

  /**
   * Gets the type of data that this source is providing.
   */
  DataType<T> getDataType();

  /**
   * Closes this data source and frees any used resources.
   */
  default void close() {
    // default to NOP
  }

  /**
   * Gets the type of this source.
   */
  SourceType getType();

  default String getId() {
    return getType().toUri(getName());
  }

  /**
   * Connects this source to the underlying data stream.
   */
  void connect();

  /**
   * Disconnects this source from the underlying data stream. Any changes made to this source will
   * not propagate to the data stream, and changes to the data stream will not affect this source.
   * This can be reconnected at any time with {@link #connect()}.
   */
  void disconnect();

  /**
   * Checks if this source is currently connected to its underlying data stream.
   */
  boolean isConnected();

}
