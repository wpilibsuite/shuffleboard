package edu.wpi.first.shuffleboard.app.json;

import edu.wpi.first.shuffleboard.api.data.IncompatibleSourceException;
import edu.wpi.first.shuffleboard.api.properties.SavePropertyFrom;
import edu.wpi.first.shuffleboard.api.properties.SaveThisProperty;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.util.ReflectionUtils;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.api.widget.Widget;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.Property;

@AnnotatedTypeAdapter(forType = Widget.class)
public class WidgetSaver implements ElementTypeAdapter<Widget> {

  private static final Logger log = Logger.getLogger(WidgetSaver.class.getName());

  private final SourcedRestorer sourcedRestorer = new SourcedRestorer();

  @Override
  public JsonElement serialize(Widget src, JsonSerializationContext context) {
    JsonObject object = new JsonObject();
    object.addProperty("_type", src.getName());
    for (int i = 0; i < src.getSources().size(); i++) {
      object.addProperty("_source" + i, src.getSources().get(i).getId());
    }
    object.addProperty("_title", src.getTitle());

    final List<Property<?>> savedProperties = getPropertyFields(src)
        .map(f -> ReflectionUtils.<Property<?>>getUnchecked(src, f))
        .collect(Collectors.toList());

    // Save exported properties
    for (Property p : src.getProperties()) {
      if (!savedProperties.contains(p)) {
        serializeProperty(context, object, p, p.getName());
      }
    }

    // Save @SaveThisProperty fields
    getPropertyFields(src)
        .forEach(f -> {
          Property<?> property = ReflectionUtils.getUnchecked(src, f);
          String name = getSavedName(property, f.getAnnotation(SaveThisProperty.class));
          serializeProperty(context, object, property, name);
        });

    // Save @SavePropertyFrom fields
    Arrays.stream(src.getClass().getDeclaredFields())
        .filter(f -> f.getAnnotationsByType(SavePropertyFrom.class).length > 0)
        .forEach(f -> {
          SavePropertyFrom[] annotations = f.getAnnotationsByType(SavePropertyFrom.class);
          for (SavePropertyFrom annotation : annotations) {
            if (annotation.propertyName().isEmpty()) {
              throw new IllegalArgumentException("No property name was specified");
            }
            String savedName = getPropertyName(annotation);
            String propName = annotation.propertyName();
            try {
              Object field = ReflectionUtils.getUnchecked(src, f);
              Method getter = getGetter(f.getType(), propName);
              Object value = getter.invoke(field);
              object.add(savedName, context.serialize(value, getter.getReturnType()));
            } catch (ReflectiveOperationException e) {
              throw new RuntimeException("Could not get value of property '" + propName + "' of " + f.getType(), e);
            }
          }
        });

    return object;
  }

  @Override
  public Widget deserialize(JsonElement json, JsonDeserializationContext context) throws JsonParseException {
    JsonObject obj = json.getAsJsonObject();
    String type = obj.get("_type").getAsString();
    Widget widget = Components.getDefault().createWidget(type)
        .orElseThrow(() -> new JsonParseException("No widget found for " + type));

    for (int i = 0; i > Integer.MIN_VALUE; i++) {
      String prop = "_source" + i;
      if (obj.has(prop)) {
        String uri = obj.get(prop).getAsString();
        Optional<? extends DataSource<?>> existingSource = Sources.getDefault().get(uri);
        try {
          if (existingSource.isPresent()) {
            widget.addSource(existingSource.get());
          } else {
            // Attempt to create a new source for the saved URI
            DataSource<?> dataSource = SourceTypes.getDefault().forUri(uri);
            // Check the source type to make sure it's the same as the one expected in the save file
            if (dataSource.getType().equals(SourceTypes.getDefault().typeForUri(uri))) {
              widget.addSource(dataSource);
            } else {
              log.warning("Saved source type is not present, adding destroyed source(s) instead");
              sourcedRestorer.addDestroyedSourcesForAllDataTypes(widget, uri);
            }
          }
        } catch (IncompatibleSourceException e) {
          log.log(Level.WARNING, "Couldn't load source, adding destroyed source(s) instead", e);
          sourcedRestorer.addDestroyedSourcesForAllDataTypes(widget, uri);
        }
      } else {
        break;
      }
    }

    JsonElement title = obj.get("_title");
    if (title != null) {
      widget.setTitle(title.getAsString());
    }

    List<Property<?>> savedProperties = getPropertyFields(widget)
        .map(f -> ReflectionUtils.<Property<?>>getUnchecked(widget, f))
        .collect(Collectors.toList());

    // Load exported properties
    for (Property p : widget.getProperties()) {
      if (savedProperties.contains(p)) {
        continue;
      }
      Object deserialized = context.deserialize(obj.get(p.getName()), p.getValue().getClass());
      if (deserialized != null) {
        p.setValue(deserialized);
      }
    }

    // Load @SaveThisProperty fields
    getPropertyFields(widget)
        .forEach(f -> {
          Property property = ReflectionUtils.getUnchecked(widget, f);
          String name = getSavedName(property, f.getAnnotation(SaveThisProperty.class));
          Object deserialize = context.deserialize(obj.get(name), property.getValue().getClass());
          property.setValue(deserialize);
        });

    // Load @SavePropertyFrom fields
    Arrays.stream(widget.getClass().getDeclaredFields())
        .filter(f -> f.getAnnotationsByType(SavePropertyFrom.class).length > 0)
        .forEach(f -> {
          SavePropertyFrom[] annotations = f.getAnnotationsByType(SavePropertyFrom.class);
          for (SavePropertyFrom annotation : annotations) {
            if (annotation.propertyName().isEmpty()) {
              throw new IllegalArgumentException("No property name set");
            }
            String savedName = getPropertyName(annotation);
            String propName = annotation.propertyName();
            try {
              Object field = ReflectionUtils.getUnchecked(widget, f);
              Method set = getSetter(f.getType(), propName);
              Object deserialize = context.deserialize(obj.get(savedName), set.getParameterTypes()[0]);
              set.invoke(field, deserialize);
            } catch (IllegalAccessException | InvocationTargetException e) {
              throw new RuntimeException("Could not get value of property '" + propName + "' of " + f.getType(), e);
            }
          }
        });

    return widget;
  }

  /**
   * Gets the getter method in the given class for a property with the given name, in the form {@code get<name>} e.g.
   * {@code getFoo()}. The method must be public. Boolean properties can also be in the form {@code is<name>}, e.g.
   * {@code isFoo()}.
   *
   * @param clazz        the class containing the property
   * @param propertyName the name of the property
   *
   * @return the getter method for the property
   *
   * @throws IllegalArgumentException if there is not exactly ONE method matching {@code public Foo get<property>()}
   *                                  or {@code public boolean is<property>()}
   */
  static Method getGetter(Class<?> clazz, String propertyName) {
    // Convert the first character to uppercase, so property name "foo" becomes "getFoo" or "isFoo"
    String base = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    String getterName = "get" + base;
    String isName = "is" + base;
    List<Method> possibleGetters = Arrays.stream(clazz.getMethods())
        .filter(m -> m.getName().equals(getterName) || isBooleanGetterWithName(m, isName))
        .filter(m -> m.getParameterCount() == 0)
        .collect(Collectors.toList());
    if (possibleGetters.isEmpty()) {
      throw new IllegalArgumentException("No getters for property '" + propertyName + "' in " + clazz);
    } else if (possibleGetters.size() > 1) {
      throw new IllegalArgumentException("Multiple getters for property " + propertyName + ": " + possibleGetters);
    }
    return possibleGetters.get(0);
  }

  private static boolean isBooleanGetterWithName(Method method, String mName) {
    return (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)
        && method.getName().equals(mName);
  }

  /**
   * Gets the setter method in the given class for a property with the given name, in the form {@code set<name>} e.g.
   * {@code setFoo()}. The method must be public, and cannot have any overrides (for example
   * {@code public void setFoo(Foo)} and {@code public void setFoo(Bar)} would cause problems).
   *
   * @param clazz        the class containing the property
   * @param propertyName the name of the property
   *
   * @return the setter method for the property
   *
   * @throws IllegalArgumentException if there is not exactly ONE method matching
   *                                  {@code public void set<property>(<type>)}
   */
  static Method getSetter(Class<?> clazz, String propertyName) {
    // Convert the first character to uppercase, so property name "foo" becomes "setFoo"
    String setterName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    List<Method> possibleSetters = Arrays.stream(clazz.getMethods())
        .filter(m -> m.getName().equals(setterName))
        .filter(m -> m.getParameterCount() == 1)
        .filter(m -> m.getReturnType() == void.class)
        .collect(Collectors.toList());
    if (possibleSetters.isEmpty()) {
      throw new IllegalArgumentException("No setter for property " + propertyName + " in " + clazz.getName());
    } else if (possibleSetters.size() > 1) {
      throw new IllegalArgumentException("Too many setters for property " + propertyName + ": "
          + possibleSetters.stream()
          .map(Method::getName)
          .collect(Collectors.joining(", ")));
    }
    return possibleSetters.get(0);
  }

  /**
   * Gets a stream of the fields in the widget's class annotated with {@link SaveThisProperty @SaveThisProperty} that
   * are not available in its {@link Widget#getProperties() properties list}.
   */
  static Stream<Field> getPropertyFields(Widget widget) {
    return Arrays.stream(widget.getClass().getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(SaveThisProperty.class))
        .filter(f -> Property.class.isAssignableFrom(f.getType()))
        .peek(f -> f.setAccessible(true));
  }

  static void serializeProperty(JsonSerializationContext context, JsonObject object, Property p, String name) {
    object.add(name, context.serialize(p.getValue()));
  }

  /**
   * Gets the saved name a property annotated with {@code @SaveThisProperty}. If the name specified in the annotation
   * is an empty string (the default value), then the property's name will be used. Otherwise, the saved name will be
   * the one specified in the annotation.
   *
   * @param property   the property to get the saved name of
   * @param annotation the annotation on the property's field
   *
   * @return the saved name of the property
   *
   * @throws IllegalArgumentException if the property has no name (either null or empty string) and the annotation does
   *                                  not specify the property name
   */
  static String getSavedName(Property<?> property, SaveThisProperty annotation) {
    if (annotation.name().isEmpty()) {
      if (property.getName() == null || property.getName().isEmpty()) {
        throw new IllegalArgumentException("The property has no name, and no name was specified in the annotation");
      }
      return property.getName();
    } else {
      return annotation.name();
    }
  }

  /**
   * Gets the saved name of a property saved from an annotated property field.
   *
   * @param annotation the annotation to get the name of the property from
   *
   * @return the saved name of the property as set by the annotation
   */
  static String getPropertyName(SavePropertyFrom annotation) {
    if (annotation.savedName().isEmpty()) {
      return annotation.propertyName(); // Don't need to check this - it's done before this method is called
    } else {
      return annotation.savedName();
    }
  }

}
