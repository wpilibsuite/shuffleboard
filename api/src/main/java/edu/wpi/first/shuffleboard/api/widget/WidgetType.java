package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;

import java.util.Set;

/**
 * WidgetType extends ComponentType with Widget-specific metadata.
 */
public interface WidgetType extends ComponentType<Widget> {
  /**
   * Get data types the widget should be suggested for.
   */
  Set<DataType> getDataTypes();

  /**
   * Creates a widget type for a widget class that has a {@link Description @Description} annotation.
   *
   * @param widgetClass the widget class
   */
  static <T extends AnnotatedWidget> WidgetType forAnnotatedWidget(Class<T> widgetClass) {
    Components.validateAnnotatedComponentClass(widgetClass);
    return new AbstractWidgetType(widgetClass.getAnnotation(Description.class)) {
      @Override
      public Widget get() {
        return Components.viewFor(widgetClass).orElseGet(() -> {
          try {
            return widgetClass.newInstance();
          } catch (Exception e) {
            throw new RuntimeException("The widget class could not be instantiated", e);
          }
        });
      }
    };
  }

}
