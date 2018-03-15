package edu.wpi.first.shuffleboard.api.sources;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.properties.AsyncProperty;
import edu.wpi.first.shuffleboard.api.properties.AtomicBooleanProperty;
import edu.wpi.first.shuffleboard.api.widget.Sourced;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import static java.util.Objects.requireNonNull;

/**
 * Abstract implementation of DataSource that defines properties for {@link #nameProperty()},
 * {@link #activeProperty()}, and {@link #dataProperty()} for subclasses.
 *
 * @param <T> the type of data this source provides
 */
public abstract class AbstractDataSource<T> implements DataSource<T> {

  private final Set<Sourced> clients = Collections.newSetFromMap(new WeakHashMap<>());
  protected final StringProperty name = new SimpleStringProperty(this, "name", "");
  protected final BooleanProperty active = new AtomicBooleanProperty(this, "active", false);
  protected final Property<T> data = new AsyncProperty<>(this, "data", null);
  protected final BooleanProperty connected = new AtomicBooleanProperty(this, "connected", false);
  protected final DataType<T> dataType;

  protected AbstractDataSource(DataType<T> dataType) {
    this.dataType = requireNonNull(dataType, "dataType");
    this.data.setValue(dataType.getDefaultValue());
  }

  @Override
  public StringProperty nameProperty() {
    return name;
  }

  @Override
  public BooleanProperty activeProperty() {
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
  public DataType<T> getDataType() {
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

  @Override
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

  @Override
  public void addClient(Sourced client) {
    clients.add(client);
  }

  @Override
  public void removeClient(Sourced client) {
    clients.remove(client);
    if (clients.isEmpty()) {
      close();
    }
  }

  @Override
  public boolean hasClients() {
    return !clients.isEmpty();
  }

}
