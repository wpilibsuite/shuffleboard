package edu.wpi.first.shuffleboard.app.widget;

import com.google.common.reflect.ClassPath;
import edu.wpi.first.shuffleboard.api.widget.AnnotatedWidget;
import edu.wpi.first.shuffleboard.api.widget.Description;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import java.io.IOException;

/**
 * A helper class that loads the stock widgets.
 */
public final class StockWidgets {

  private StockWidgets() {
  }

  /**
   * Initializes and registers all stock widgets.
   */
  @SuppressWarnings("unchecked")
  public static void init() {
    try {
      ClassPath.from(StockWidgets.class.getClassLoader())
               .getAllClasses()
               .stream()
               .filter(ci -> ci.getPackageName().startsWith("edu.wpi.first.shuffleboard"))
               .map(ClassPath.ClassInfo::load)
               .filter(AnnotatedWidget.class::isAssignableFrom)
               .map(c -> (Class<Widget>) c)
               .filter(c -> c.isAnnotationPresent(Description.class))
               .forEach(Widgets::register);
    } catch (IOException e) {
      throw new RuntimeException("Could not initialize stock widgets", e);
    }
  }

}
