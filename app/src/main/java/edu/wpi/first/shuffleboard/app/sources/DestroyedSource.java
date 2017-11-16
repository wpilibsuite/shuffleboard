package edu.wpi.first.shuffleboard.app.sources;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * A type of data source that represents the state of another source that has been destroyed or removed as a result
 * of its defining plugin being unloaded. The restored data source may depend on a data type no longer defined, in which
 * case a {@link DataTypeChangedException} will be thrown when it is attempted to be restored.
 */
public class DestroyedSource<T> implements DataSource<T> {

  private final DataType dataType;
  private final String oldId;
  private final StringProperty name = new SimpleStringProperty(this, "name", null);
  private final ObjectProperty<T> data = new SimpleObjectProperty<>(this, "data", null);
  private final BooleanProperty active = new SimpleBooleanProperty(this, "active", false);

  /**
   * Creates a new destroyed source for the given data type and URI. This should be used to represent a saved data
   * source whose data or type is unknown at the time it is loaded.
   *
   * @param dataType the data type that the restored source should provide
   * @param uri      the URI of the real source corresponding to the created one
   * @param <T>      the type of data in the source
   */
  public static <T> DestroyedSource<T> forUnknownData(DataType<T> dataType, String uri) {
    return new DestroyedSource<>(dataType, uri, dataType.getDefaultValue());
  }

  /**
   * Creates a new instance that can restore a data source.
   *
   * @param dataType the type of the data the destroyed source provides
   * @param id       the ID of the destroyed source
   * @param data     the data of the source when it was destroyed, or {@code null} if no data was present or known
   */
  public DestroyedSource(DataType<T> dataType, String id, T data) {
    this.dataType = dataType;
    this.oldId = id;
    this.name.set(SourceTypes.getDefault().stripProtocol(id));
    this.data.set(data);
  }

  /**
   * Creates a new instance that can restore the given data source.
   *
   * @param destroyed the destroyed source that the new instance should be able to restore.
   */
  public DestroyedSource(DataSource<T> destroyed) {
    this(destroyed.getDataType(), destroyed.getId(), destroyed.getData());
    this.name.set(destroyed.getName());
  }

  private SourceType getSourceType() {
    return SourceTypes.getDefault().typeForUri(oldId);
  }

  /**
   * Creates a data source identical to the original.
   *
   * @throws DataTypeChangedException if the data type of the restored source is different from the saved one
   * @throws IllegalStateException    if the saved source type is not registered when this method is called
   */
  @SuppressWarnings("unchecked")
  public DataSource<T> restore() throws DataTypeChangedException, IllegalStateException {
    SourceType sourceType = getSourceType();
    if (SourceTypes.getDefault().isRegistered(sourceType)) {
      DataSource<T> restored = (DataSource<T>) sourceType.forUri(oldId);
      if (!restored.getDataType().equals(dataType)) {
        throw new DataTypeChangedException(
            "The new data type is " + restored.getDataType() + ", was expecting " + dataType);
      }
      restored.nameProperty().set(name.get());
      restored.activeProperty().set(true);
      restored.setData(getData());
      return restored;
    } else {
      throw new IllegalStateException("The source type " + sourceType.getName() + " is not registered");
    }
  }

  @Override
  public BooleanProperty activeProperty() {
    return active;
  }

  @Override
  public StringProperty nameProperty() {
    return name;
  }

  @Override
  public Property<T> dataProperty() {
    return data;
  }

  @Override
  public void setData(T newValue) {
    // NOP
  }

  @Override
  public DataType<T> getDataType() {
    return dataType;
  }

  @Override
  public SourceType getType() {
    return getSourceType();
  }

  @Override
  public void connect() {
    // Nothing to connect to
  }

  @Override
  public void disconnect() {
    // Nothing to disconnect from
  }

  @Override
  public boolean isConnected() {
    return false;
  }

}
