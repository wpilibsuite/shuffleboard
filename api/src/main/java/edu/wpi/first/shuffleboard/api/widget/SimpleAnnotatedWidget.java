package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

import org.fxmisc.easybind.monadic.MonadicBinding;
import org.fxmisc.easybind.monadic.PropertyBinding;

import javafx.beans.property.Property;

public abstract class SimpleAnnotatedWidget<T> extends SingleSourceWidget
    implements AnnotatedWidget, SingleTypeWidget<T> {

  /**
   * A read-only binding of the data for this widget. If this widget has a source, this is equivalent to
   * {@link #dataProperty()}; otherwise, it contains the default value of this widgets data type.
   */
  protected final MonadicBinding<T> dataOrDefault = dataProperty().orElse(getDataType().getDefaultValue());

  @Override
  public final PropertyBinding<T> dataProperty() {
    return super.dataProperty();
  }

  @Override
  public T getData() {
    return SingleTypeWidget.super.getData();
  }

  public final Property<DataSource<T>> typedSourceProperty() {
    return (Property) sourceProperty();
  }

}
