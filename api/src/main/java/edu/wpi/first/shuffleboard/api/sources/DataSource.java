package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.widget.Sourced;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

/**
 * A data source provides some kind of data that widgets can display and manipulate. It can be
 * active or inactive; active sources may update at any time, while inactive sources are guaranteed
 * to never update. This can be useful if a widget wants to disable user control while the source
 * can't handle it.
 *
 * <p>Data sources have several major properties:
 * <ul>
 * <li>active</li>
 * <li>name</li>
 * <li>ID</li>
 * <li>data</li>
 * <li>data type</li>
 * <li>type</li>
 * <li>connectedness</li>
 * </ul>
 *
 * <p>The <i>active</i> property describes whether or not the source is connected to the backing data source and can
 * update at any time. An active source is always <i>connected</i>.
 *
 * <p>The <i>name</i> property is a unique identifier for the data backing the source. The name of the source also
 * describes a <i>path</i> to the data and may be thought of like a filesystem structure. Forward slashes ({@code "/"})
 * delimit the separate components. If there are multiple components, each one is expected to be its own valid source
 * for that type of data. For example, a theoretical "file system" data source with the name {@code /home/usr/foo.txt}
 * would have a "File Source" for the text file, along with a "Directory Source" for each of {@code /home/} and
 * {@code /home/usr}.
 *
 * <p>The <i>ID</i> of a data source is simply its name encoded in a URI-like string, prefixed by a unique identifier
 * string for its type. {@code "${protocol}://${name}"}, eg {@code "file:///home/usr/foo.txt} or
 * {@code network_table:///SmartDashboard/}.
 *
 * <p>The <i>data type</i> of a source contains the <i>expected type of the data</i>. The <i>actual</i> data may not be
 * of this type; in this case, the source is marked as <i>inactive</i> (but still <i>connected</i>) until the actual
 * data has this type.
 *
 * <p>The <i>data</i> property contains the current data for the source. This may also be set by user-controllable
 * widgets to manipulate the data.
 *
 * <p>The <i>type</i> of a data source defines how the source behaves and where its data comes from. More information
 * can be found {@link SourceType here}.
 *
 * <p><i>Connectedness</i> simply marks whether or not the source is connected to the actual data source. For example,
 * sources whose data resides on a remote resource like a server are not connected if there is no network connection to
 * that remote resource.
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
   * Closes this data source and frees any used resources. A closed source will not be usable
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

  BooleanProperty connectedProperty();

  /**
   * Checks if this source is currently connected to its underlying data stream.
   */
  boolean isConnected();

  /**
   * Checks if any clients are connected to this source.
   */
  boolean hasClients();

  /**
   * Adds a client to this source.
   *
   * @param client the client to add
   */
  void addClient(Sourced client);

  /**
   * Removes a client from this source. If there are no remaining clients after removing a client, the source will be
   * {@link #close() closed}.
   *
   * @param client the client to remove
   */
  void removeClient(Sourced client);

}
