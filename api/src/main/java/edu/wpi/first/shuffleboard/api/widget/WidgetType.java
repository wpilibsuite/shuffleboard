package edu.wpi.first.shuffleboard.api.widget;

import edu.wpi.first.shuffleboard.api.data.DataType;

import java.util.Optional;
import java.util.Set;

/**
 * WidgetType extends ComponentType with Widget-specific metadata.
 */
public interface WidgetType<W extends Widget> extends ComponentType<W> {
  /**
   * Get data types the widget should be suggested for.
   */
  Set<DataType> getDataTypes();

  /**
   * Creates a widget type for a widget class that has a {@link Description @Description} annotation.
   *
   * @param widgetClass the widget class
   */
  static <T extends Widget> WidgetType<T> forAnnotatedWidget(Class<T> widgetClass) {
    Components.validateAnnotatedComponentClass(widgetClass);

    return new AbstractWidgetType<T>(widgetClass.getAnnotation(Description.class)) {
      @Override
      public T get() throws ComponentInstantiationException {
        try {
          Optional<T> view = Components.viewFor(widgetClass);
          if (view.isPresent()) {
            return view.get();
          } else {
            return widgetClass.newInstance();
          }
        } catch (ReflectiveOperationException | RuntimeException e) {
          throw new ComponentInstantiationException(this, e);
        }
      }

      @Override
      public Class<T> getType() {
        return widgetClass;
      }
    };
  }

}
