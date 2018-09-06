package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;
import org.fxmisc.easybind.monadic.PropertyBinding;

import javafx.beans.property.Property;

public abstract class SimpleAnnotatedWidget<T> extends SingleSourceWidget
    implements AnnotatedWidget, SingleTypeWidget<T> {

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

  @Override
  public final PropertyBinding<T> dataProperty() {
    return data;
  }

  public final Property<DataSource<T>> typedSourceProperty() {
    return (Property) sourceProperty();
  }

}
