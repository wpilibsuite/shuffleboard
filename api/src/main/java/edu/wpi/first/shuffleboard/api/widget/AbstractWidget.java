package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.sources.DataSource;

import com.google.common.collect.ImmutableList;

import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 * A partial implementation of {@link Widget} that implements property methods.
 */
public abstract class AbstractWidget implements Widget {

  protected final ObservableList<DataSource> sources = FXCollections.observableArrayList();

  private final StringProperty title = new SimpleStringProperty(this, "title", "");

  private final ChangeListener<Boolean> connectionListener = (__, was, is) -> {
    if (is) {
      getView().setDisable(!sources.stream().allMatch(DataSource::isConnected));
    } else {
      getView().setDisable(true);
    }
  };

  /**
   * Only set the title when the sources change if it hasn't been set externally (eg by a user or an enclosing layout).
   */
  private boolean useGeneratedTitle = true;

  protected AbstractWidget() {
    sources.addListener((InvalidationListener) __ -> {
      if (getTitle().isEmpty()) {
        useGeneratedTitle = true;
      }
      if (useGeneratedTitle) {
        if (sources.size() == 1) {
          title.set(sources.get(0).getName());
        } else {
          title.set(getName() + " (" + sources.size() + " sources)");
        }
      }
      getView().setDisable(sources.stream().anyMatch(s -> !s.isConnected()));
    });
    sources.addListener((ListChangeListener<DataSource>) c -> {
      getView().setDisable(!sources.stream().allMatch(DataSource::isConnected));
      while (c.next()) {
        if (c.wasAdded()) {
          c.getAddedSubList().forEach(s -> s.connectedProperty().addListener(connectionListener));
        } else if (c.wasRemoved()) {
          c.getRemoved().forEach(s -> s.connectedProperty().removeListener(connectionListener));
        }
      }
    });
  }

  @Override
  public void setTitle(String title) {
    Widget.super.setTitle(title);
    useGeneratedTitle = title == null || title.isEmpty();
  }

  @Override
  public List<Group> getSettings() {
    return ImmutableList.of();
  }

  @Override
  public StringProperty titleProperty() {
    return title;
  }

  @Override
  public final ObservableList<DataSource> getSources() {
    return sources;
  }

  @Override
  public void addSource(DataSource source) throws IncompatibleSourceException {
    if (!getDataTypes().contains(source.getDataType())) {
      throw new IncompatibleSourceException(getDataTypes(), source.getDataType());
    }
    sources.add(source);
    source.addClient(this);
  }

}
