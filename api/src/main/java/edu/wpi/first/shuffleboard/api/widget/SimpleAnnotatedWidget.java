package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

import org.fxmisc.easybind.EasyBind;

import javafx.beans.property.Property;

public abstract class SimpleAnnotatedWidget<T> extends SingleSourceWidget
    implements AnnotatedWidget, SingleTypeWidget<T> {

  /**
   * The property for this widgets data. This is the preferred way to get the current value of the
   * data source because it will update whenever the source is modified.
   */
  private final Property<T> data
      = EasyBind.monadic(source).selectProperty(DataSource::dataProperty);

  @Override
  public final Property<T> dataProperty() {
    return data;
  }

}
