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
                  Map<String, Object> properties) {
    super(path, parent, displayType, properties);
    this.sourceSupplier = sourceSupplier;
  }

  @Override
  public DataSource<?> getDataSource() {
    return sourceSupplier.get();
  }

}
