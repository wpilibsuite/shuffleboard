package edu.wpi.first.shuffleboard.api.tab.model;

import edu.wpi.first.shuffleboard.api.sources.DataSource;

import java.util.Map;
import java.util.function.Supplier;

class WidgetModelImpl extends ComponentModelImpl implements WidgetModel {

  private final Supplier<? extends DataSource<?>> sourceSupplier;

  WidgetModelImpl(String path,
                  ParentModel parent,
                  Supplier<? extends DataSource<?>> sourceSupplier,
                  String displayType,
                  Map<String, Object> properties,
                  double opacity) {
    super(path, parent, displayType, properties, opacity);
    this.sourceSupplier = sourceSupplier;
  }

  @Override
  public DataSource<?> getDataSource() {
    return sourceSupplier.get();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof WidgetModelImpl)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    WidgetModelImpl that = (WidgetModelImpl) o;

    return sourceSupplier != null ? sourceSupplier.equals(that.sourceSupplier) : that.sourceSupplier == null;
  }

}
