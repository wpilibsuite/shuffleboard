package edu.wpi.first.shuffleboard.widget;

import com.google.common.reflect.ClassPath;

import java.io.IOException;

/**
 * A helper class that defines stock widgets.
 */
public final class StockWidgets {

  private static volatile boolean didInit = false;

  private StockWidgets() {
  }

  /**
   * Initializes and registers all stock widgets.
   */
  @SuppressWarnings("unchecked")
  public static void init() throws IOException {
    if (didInit) {
      return;
    }
    ClassPath.from(StockWidgets.class.getClassLoader())
             .getAllClasses()
             .stream()
             .filter(ci -> ci.getPackageName().startsWith("edu.wpi.first.shuffleboard"))
             .map(ClassPath.ClassInfo::load)
             .filter(SimpleAnnotatedWidget.class::isAssignableFrom)
             .map(c -> (Class<Widget>) c)
             .filter(c -> c.isAnnotationPresent(Description.class))
             .forEach(Widgets::register);
    didInit = true;
  }

}
