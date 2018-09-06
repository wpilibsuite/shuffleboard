package edu.wpi.first.shuffleboard.api.json;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation for TypeAdapters that indicates it should be included as part of any Gson
 * configuration for this project.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AnnotatedTypeAdapter {
  /**
   * The class to call GsonBuilder#registerTypeAdapter for.
   */
  Class<?> forType();
}
