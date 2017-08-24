package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.DummySource;

import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * A partial implementation of {@code Widget} that only has a single source.
 */
public abstract class SingleSourceWidget extends AbstractWidget {

  protected final ObjectProperty<DataSource> source
      = new SimpleObjectProperty<>(this, "source", DummySource.forTypes(getDataTypes()).get());
  protected final MonadicBinding<String> sourceName;

  @SuppressWarnings("JavadocMethod")
  public SingleSourceWidget() {
    source.addListener(__ -> sources.setAll(getSource()));
    sourceName = EasyBind.monadic(source)
        .map(DataSource::getName);
  }

  @Override
  public final void addSource(DataSource source) throws IncompatibleSourceException {
    if (getDataTypes().contains(source.getDataType())) {
      this.source.set(source);
    } else {
      throw new IncompatibleSourceException(getDataTypes(), source.getDataType());
    }
  }

  public final ObjectProperty<DataSource> sourceProperty() {
    return source;
  }

  public final DataSource getSource() {
    return source.get();
  }

  public final void setSource(DataSource source) throws IncompatibleSourceException {
    addSource(source);
  }

  public final String getSourceName() {
    return sourceName.get();
  }

  public final MonadicBinding<String> sourceNameProperty() {
    return sourceName;
  }

}