package edu.wpi.first.shuffleboard.json;

import com.google.common.reflect.ClassPath;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public final class JsonBuilder {
  private static List<? extends Class<?>> typeAdapters() throws IOException {
    return ClassPath.from(JsonBuilder.class.getClassLoader())
            .getAllClasses()
            .stream()
            .filter(ci -> ci.getPackageName().startsWith("edu.wpi.first.shuffleboard.json"))
            .map(ClassPath.ClassInfo::load)
            .filter(c -> c.isAnnotationPresent(AnnotatedTypeAdapter.class))
            .collect(Collectors.toList());
  }

  public static GsonBuilder withTypeAdapter() {
    GsonBuilder builder = new GsonBuilder();

    try {
      for (Class<?> c : typeAdapters()) {
        builder.registerTypeAdapter(
                c.getAnnotation(AnnotatedTypeAdapter.class).forType(),
                c.newInstance());
      }
    } catch (IOException | InstantiationException | IllegalAccessException e) {
      e.printStackTrace();
      throw new RuntimeException("Could not initialize JSON serializers");
    }

    return builder;
  }

  private static Gson gsonInstance;
  public static Gson forSaveFile() {
    if (gsonInstance == null) {
      gsonInstance = withTypeAdapter()
              .setPrettyPrinting()
              .create();
    }

    return gsonInstance;
  }
}
