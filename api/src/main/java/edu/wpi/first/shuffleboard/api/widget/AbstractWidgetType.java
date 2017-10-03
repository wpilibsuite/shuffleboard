package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;

import java.util.Set;

/**
 * Describes a widget. This is used to make lookup of widgets easier for {@link Components}.
 */
public abstract class AbstractWidgetType implements WidgetType {

  private final String name;
  private final Set<DataType> dataTypes;

  public AbstractWidgetType(String name, Set<DataType> dataTypes) {
    this.name = name;
    this.dataTypes = dataTypes;
  }

  public AbstractWidgetType(Description description) {
    this(description.name(), DataTypes.getDefault().forJavaTypes(description.dataTypes()));
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Set<DataType> getDataTypes() {
    return dataTypes;
  }
}
