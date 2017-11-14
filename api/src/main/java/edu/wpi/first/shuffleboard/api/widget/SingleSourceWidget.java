package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.PropertyBinding;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A partial implementation of {@code Widget} that only has a single source.
 */
public abstract class SingleSourceWidget extends AbstractWidget {

  protected final ObjectProperty<DataSource> source = new SimpleObjectProperty<>(this, "source", DataSource.none());
  private final PropertyBinding<?> dataProperty;

  @SuppressWarnings("JavadocMethod")
  public SingleSourceWidget() {
    source.addListener(__ -> sources.setAll(getSource()));
    dataProperty = EasyBind.monadic(source).selectProperty(DataSource::dataProperty);
  }

  @Override
  public final void addSource(DataSource source) throws IncompatibleSourceException {
    if (getDataTypes().contains(source.getDataType())) {
      this.source.set(source);
    } else {
      throw new IncompatibleSourceException(getDataTypes(), source.getDataType());
    }
  }

  public final Property<DataSource> sourceProperty() {
    return source;
  }

  public final DataSource getSource() {
    return source.get();
  }

  public final void setSource(DataSource source) throws IncompatibleSourceException {
    addSource(source);
  }

  public Object getData() {
    return dataProperty.get();
  }

  public PropertyBinding dataProperty() {
    return dataProperty;
  }

}
