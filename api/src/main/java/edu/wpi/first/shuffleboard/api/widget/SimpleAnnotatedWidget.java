package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class SimpleAnnotatedWidget<T> extends AnnotatedWidget implements SingleTypeWidget<T> {

  /**
   * The property for this widgets data. This is the preferred way to get the current value of the
   * data source because it will update whenever the source is modified.
   */
  private final Property<T> data
      = EasyBind.monadic(source).selectProperty(DataSource::dataProperty);

  private final Property<String> sourceName = new SimpleStringProperty(this, "sourceName", "");
  private final ObservableList<Property<?>> properties = FXCollections.observableArrayList();

  // Getters and setters

  public Property<DataSource> sourceProperty() {
    return (Property) source;
  }

  public final Property<T> dataProperty() {
    return data;
  }

  public final String getSourceName() {
    return sourceName.getValue();
  }

  public final Property<String> sourceNameProperty() {
    return sourceName;
  }

}
