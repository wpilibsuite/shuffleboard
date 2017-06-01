package edu.wpi.first.shuffleboard.widget;

import java.util.Set;
import java.util.function.Supplier;

public interface WidgetType extends Supplier<Widget> {
    String getName();

    Set<DataType> getDataTypes();
}
