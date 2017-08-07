package edu.wpi.first.shuffleboard.app.json;

import com.google.common.reflect.ClassPath;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class JsonBuilder {
  private JsonBuilder() {
  }

  private static List<? extends Class<?>> typeAdapters() throws IOException {
    return ClassPath.from(JsonBuilder.class.getClassLoader())
            .getAllClasses()
            .stream()
            .filter(ci -> ci.getPackageName().startsWith("edu.wpi.first.shuffleboard.json"))
            .map(ClassPath.ClassInfo::load)
            .filter(c -> c.isAnnotationPresent(AnnotatedTypeAdapter.class))
            .collect(Collectors.toList());
  }

  /**
   * Creates a GsonBuilder instance With all of the type adapters for the current classpath.
   */
  public static GsonBuilder withTypeAdapter() {
    GsonBuilder builder = new GsonBuilder();

    try {
      for (Class<?> c : typeAdapters()) {
        builder.registerTypeAdapter(
                c.getAnnotation(AnnotatedTypeAdapter.class).forType(),
                c.newInstance());
      }
    } catch (IOException | InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Could not initialize JSON serializers", e);
    }

    return builder;
  }

  /**
   * Creates a Gson instance for use in saving and loading Shuffleboard files.
   */
  public static Gson forSaveFile() {
    return withTypeAdapter()
            .setPrettyPrinting()
            .create();
  }
}
