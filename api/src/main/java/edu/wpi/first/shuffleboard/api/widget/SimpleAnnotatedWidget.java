package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;
import org.fxmisc.easybind.monadic.PropertyBinding;

import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class SimpleAnnotatedWidget<T> extends AnnotatedWidget implements SingleTypeWidget<T> {

  /**
   * The property for this widgets data. This is the preferred way to get the current value of the
   * data source because it will update whenever the source is modified.
   */
  private final PropertyBinding<T> data
      = EasyBind.monadic(source).selectProperty(DataSource::dataProperty);

  /**
   * A read-only binding of the data for this widget. If this widget has a source, this is equivalent to
   * {@link #dataProperty()}; otherwise, it contains the default value of this widgets data type.
   */
  protected final MonadicBinding<T> dataOrDefault = data.orElse(getDataType().getDefaultValue());

  private final Property<String> sourceName = new SimpleStringProperty(this, "sourceName", "");
  private final ObservableList<Property<?>> properties = FXCollections.observableArrayList();

  // Getters and setters

  public Property<DataSource> sourceProperty() {
    return (Property) source;
  }

  @Override
  public final PropertyBinding<T> dataProperty() {
    return data;
  }

  public final String getSourceName() {
    return sourceName.getValue();
  }

  public final Property<String> sourceNameProperty() {
    return sourceName;
  }

}
