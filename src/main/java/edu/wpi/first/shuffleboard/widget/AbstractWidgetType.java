package edu.wpi.first.shuffleboard.widget;

import com.google.common.collect.ImmutableSet;

import java.util.Set;

/**
 * Describes a widget. This is used to make lookup of widgets easier for {@link Widgets}.
 */
public abstract class AbstractWidgetType implements WidgetType {

  private final String name;
  private final Set<DataType> dataTypes;

  public AbstractWidgetType(String name, Set<DataType> dataTypes) {
    this.name = name;
    this.dataTypes = dataTypes;
  }

  public AbstractWidgetType(Description description) {
    this(description.name(), ImmutableSet.copyOf(description.dataTypes()));
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
