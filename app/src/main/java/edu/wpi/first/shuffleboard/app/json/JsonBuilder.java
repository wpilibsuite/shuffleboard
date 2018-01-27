package edu.wpi.first.shuffleboard.app.json;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class JsonBuilder {

  private static final ImmutableList<Class<? extends ElementTypeAdapter<?>>> typeAdapters = ImmutableList.of(
      ColorSaver.class,
      DashboardTabPaneSaver.class,
      LayoutSaver.class,
      TileSizeSaver.class,
      WidgetPaneSaver.class,
      WidgetSaver.class
  );

  private JsonBuilder() {
  }

  /**
   * Creates a GsonBuilder instance With all of the type adapters for the current classpath.
   */
  public static GsonBuilder withTypeAdapter() {
    GsonBuilder builder = new GsonBuilder();

    try {
      for (Class<?> c : typeAdapters) {
        builder.registerTypeHierarchyAdapter(
                c.getAnnotation(AnnotatedTypeAdapter.class).forType(),
                c.newInstance());
      }
    } catch (InstantiationException | IllegalAccessException e) {
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
