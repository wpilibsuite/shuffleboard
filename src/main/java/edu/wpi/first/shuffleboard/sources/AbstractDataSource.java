package edu.wpi.first.shuffleboard.sources;

import edu.wpi.first.shuffleboard.properties.AsyncProperty;
import edu.wpi.first.shuffleboard.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.widget.DataType;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import static java.util.Objects.requireNonNull;

/**
 * Abstract implementation of DataSource that defines properties for {@link #nameProperty()},
 * {@link #activeProperty()}, and {@link #dataProperty()} for subclasses.
 *
 * @param <T> the type of data this source provides
 */
public abstract class AbstractDataSource<T> implements DataSource<T> {

  protected final Property<String> name = new SimpleStringProperty(this, "name", "");
  protected final Property<Boolean> active = new SimpleBooleanProperty(this, "active", false);
  protected final Property<T> data = new AsyncProperty<>(this, "data", null);
  protected final BooleanProperty connected = new SimpleBooleanProperty(this, "connected", false);
  protected final DataType dataType;
  private final Recorder recorder = Recorder.getInstance();

  protected AbstractDataSource(DataType dataType) {
    this.dataType = requireNonNull(dataType, "dataType");
    if (getType().isRecordable) {
      data.addListener((__, o, newData) -> {
        recorder.recordCurrentValue(this);
      });
    }
  }

  @Override
  public ObservableValue<String> nameProperty() {
    return name;
  }

  @Override
  public ObservableValue<Boolean> activeProperty() {
    return active;
  }

  @Override
  public Property<T> dataProperty() {
    return data;
  }

  protected void setName(String name) {
    this.name.setValue(name);
  }

  protected void setActive(boolean active) {
    this.active.setValue(active);
  }

  @Override
  public DataType getDataType() {
    return dataType;
  }

  @Override
  public void connect() {
    setConnected(true);
  }

  @Override
  public void disconnect() {
    setConnected(false);
  }

  public BooleanProperty connectedProperty() {
    return connected;
  }

  public void setConnected(boolean connected) {
    this.connected.set(connected);
  }

  @Override
  public boolean isConnected() {
    return connected.get();
  }

  @Override
  public String toString() {
    return String.format("%s(name=%s, active=%s, data=%s, dataType=%s)",
        getClass().getSimpleName(),
        getName(),
        isActive(),
        getData(),
        getDataType());
  }
}
