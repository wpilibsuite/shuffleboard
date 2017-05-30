package edu.wpi.first.shuffleboard.widget;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

/** Describes a widget. This is used to make lookup of widgets easier for {@link Widgets}. */
// intentionally package-private; this is not part of a public API
class WidgetDescription {

  private final String name;
  private final Set<DataType> dataTypes;

  public WidgetDescription(String name, Set<DataType> dataTypes) {
    this.name = name;
    this.dataTypes = dataTypes;
  }

  public WidgetDescription(Description description) {
    this(description.name(), ImmutableSet.copyOf(description.dataTypes()));
  }

  public WidgetDescription(Widget<?> widget) {
    this(widget.getName(), widget.getDataTypes());
  }

  public String getName() {
    return name;
  }

  public Set<DataType> getDataTypes() {
    return dataTypes;
  }
}
