package edu.wpi.first.shuffleboard.widget;

import com.google.common.reflect.ClassPath;

import java.io.IOException;

/**
 * A helper class that defines stock widgets.
 */
public final class StockWidgets {

  private StockWidgets() {
  }

  /**
   * Initializes and registers all stock widgets.
   */
  @SuppressWarnings("unchecked")
  public static void init() throws IOException {
    ClassPath.from(StockWidgets.class.getClassLoader())
             .getAllClasses()
             .stream()
             .filter(ci -> ci.getPackageName().startsWith("edu.wpi.first.shuffleboard"))
             .map(ClassPath.ClassInfo::load)
             .filter(SimpleAnnotatedWidget.class::isAssignableFrom)
             .map(c -> (Class<Widget>) c)
             .filter(c -> c.isAnnotationPresent(Description.class))
             .forEach(Widgets::register);
  }

}
