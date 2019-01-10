package edu.wpi.first.shuffleboard.api.json;

import edu.wpi.first.shuffleboard.api.prefs.Group;
import edu.wpi.first.shuffleboard.api.prefs.Setting;
import edu.wpi.first.shuffleboard.api.properties.SavePropertyFrom;
import edu.wpi.first.shuffleboard.api.properties.SaveThisProperty;
import edu.wpi.first.shuffleboard.api.util.ReflectionUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;
import edu.wpi.first.shuffleboard.api.widget.Component;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

/**
 * Saves property fields annotated with {@link SaveThisProperty @SaveThisProperty} and properties of fields annotated
 * with {@link SavePropertyFrom @SavePropertyFrom}. Annotated fields from the entire superclass hierarchy are saved,
 * not just fields from the object being serialized.
 */
public final class PropertySaver {

  /**
   * Saves all properties from a component. Properties are saved in this order:
   * <ol>
   * <li>Properties exported via {@link Component#getSettings()}</li>
   * <li>Properties from fields annotated with {@link SaveThisProperty} in the component's class hierarchy</li>
   * <li>Properties from fields annotated with {@link SavePropertyFrom} in the component's class hierarchy</li>
   * </ol>
   *
   * @param object     the object to serialize properties from
   * @param context    the serialization context
   * @param jsonObject the JSON object to serialize into
   */
  public void saveAllProperties(Component object, JsonSerializationContext context, JsonObject jsonObject) {
    final List<Property<?>> savedProperties = getPropertyFields(object.getClass())
        .map(f -> ReflectionUtils.<Property<?>>getUnchecked(object, f))
        .collect(Collectors.toList());

    // Save settings
    for (Group group : object.getSettings()) {
      for (Setting<?> setting : group.getSettings()) {
        var property = setting.getProperty();
        if (!savedProperties.contains(property)) {
          Class<?> type = setting.getType() == null ? setting.getProperty().getClass() : setting.getType();
          serializeProperty(context, jsonObject, property, type,group.getName() + "/" + setting.getName());
        }
      }
    }

    saveAnnotatedFields(object, context, jsonObject);
    saveNestedProperties(object, context, jsonObject);
  }

  /**
   * Saves property fields annotated with {@link SaveThisProperty @SaveThisProperty}.
   *
   * @param object     the object to serialize properties from
   * @param context    the serialization context
   * @param jsonObject the JSON object to serialize into
   */
  public void saveAnnotatedFields(Object object, JsonSerializationContext context, JsonObject jsonObject) {
    getPropertyFields(object.getClass())
        .forEach(f -> {
          Property<?> property = ReflectionUtils.getUnchecked(object, f);
          String name = getSavedName(property, f.getAnnotation(SaveThisProperty.class));
          serializeProperty(context, jsonObject, property, property.getValue().getClass(), name);
        });
  }

  /**
   * Saves nested properties from fields annotated ith {@link SavePropertyFrom @SavePropertyFrom} in the object's class
   * hierarchy.
   *
   * @param object     the object to serialize properties from
   * @param context    the serialization context
   * @param jsonObject the JSON object to serialize into
   */
  public void saveNestedProperties(Object object, JsonSerializationContext context, JsonObject jsonObject) {
    getNestedPropertyFields(object.getClass())
        .forEach(f -> {
          SavePropertyFrom[] annotations = f.getAnnotationsByType(SavePropertyFrom.class);
          for (SavePropertyFrom annotation : annotations) {
            if (annotation.propertyName().isEmpty()) {
              throw new IllegalArgumentException("No property name was specified");
            }
            String savedName = getPropertyName(annotation);
            String propName = annotation.propertyName();
            try {
              Object field = ReflectionUtils.getUnchecked(object, f);
              Method getter = getGetter(f.getType(), propName);
              Object value = getter.invoke(field);
              jsonObject.add(savedName, context.serialize(value, getter.getReturnType()));
            } catch (ReflectiveOperationException e) {
              throw new RuntimeException("Could not get value of property '" + propName + "' of " + f.getType(), e);
            }
          }
        });
  }

  /**
   * Read all properties from a component. Properties are loaded in this order:
   * <ol>
   * <li>Properties exported via {@link Component#getSettings()}</li>
   * <li>Properties from fields annotated with {@link SaveThisProperty} in the component's class hierarchy</li>
   * <li>Properties from fields annotated with {@link SavePropertyFrom} in the component's class hierarchy</li>
   * </ol>
   *
   * @param object     the object to serialize properties from
   * @param context    the serialization context
   * @param jsonObject the JSON object to serialize into
   */
  public void readAllProperties(Component object, JsonDeserializationContext context, JsonObject jsonObject) {
    List<Property<?>> savedProperties = getPropertyFields(object.getClass())
        .map(f -> ReflectionUtils.<Property<?>>getUnchecked(object, f))
        .collect(Collectors.toList());

    // Load settings
    for (Group group : object.getSettings()) {
      for (Setting setting : group.getSettings()) {
        var property = setting.getProperty();
        if (savedProperties.contains(property)) {
          continue;
        }
        var type = setting.getType() == null ? property.getValue().getClass() : setting.getType();
        type = TypeUtils.primitiveForBoxedType(type);
        Object deserialized = context.deserialize(
            jsonObject.get(group.getName() + "/" + setting.getName()), type);
        if (deserialized != null) {
          setting.setValue(deserialized);
        }
      }
    }

    readAnnotatedFields(object, context, jsonObject);
    readNestedProperties(object, context, jsonObject);
  }

  /**
   * Deserializes property fields annotated with {@link SaveThisProperty @SaveThisProperty}.
   *
   * @param object     the object containing the properties to deserialize
   * @param context    the deserialization context
   * @param jsonObject the JSON object to deserialize
   */
  public void readAnnotatedFields(Object object, JsonDeserializationContext context, JsonObject jsonObject) {
    getPropertyFields(object.getClass())
        .forEach(f -> {
          Property property = ReflectionUtils.getUnchecked(object, f);
          String name = getSavedName(property, f.getAnnotation(SaveThisProperty.class));
          Object deserialize = context.deserialize(jsonObject.get(name), property.getValue().getClass());
          // Can be null if not set
          // Instead of throwing IllegalArgumentException, just let the default value be used
          if (deserialize != null) {
            property.setValue(deserialize);
          }
        });
  }

  /**
   * Deserialized nested properties from fields annotated ith {@link SavePropertyFrom @SavePropertyFrom} in the object's
   * class hierarchy.
   *
   * @param object     the object containing the properties to deserialize
   * @param context    the deserialization context
   * @param jsonObject the JSON object to deserialize
   */
  public void readNestedProperties(Object object, JsonDeserializationContext context, JsonObject jsonObject) {
    getNestedPropertyFields(object.getClass())
        .forEach(f -> {
          SavePropertyFrom[] annotations = f.getAnnotationsByType(SavePropertyFrom.class);
          for (SavePropertyFrom annotation : annotations) {
            if (annotation.propertyName().isEmpty()) {
              throw new IllegalArgumentException("No property name set");
            }
            String savedName = getPropertyName(annotation);
            String propName = annotation.propertyName();
            try {
              Object field = ReflectionUtils.getUnchecked(object, f);
              Method set = getSetter(f.getType(), propName);
              Object deserialize = context.deserialize(jsonObject.get(savedName), set.getParameterTypes()[0]);
              // Can be null if not set
              // Instead of throwing IllegalArgumentException, just let the default value be used
              if (deserialize != null) {
                set.invoke(field, deserialize);
              }
            } catch (IllegalAccessException | InvocationTargetException e) {
              throw new RuntimeException("Could not get value of property '" + propName + "' of " + f.getType(), e);
            }
          }
        });
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
  public static Method getGetter(Class<?> clazz, String propertyName) {
    // Convert the first character to uppercase, so property name "foo" becomes "getFoo" or "isFoo"
    String base = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
    String getterName = "get" + base;
    String isName = "is" + base; // NOPMD linguistics antipattern - this is the name of a boolean "isX" method
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
  private static Method getSetter(Class<?> clazz, String propertyName) {
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
   * Gets a stream of the fields in the object's class hierarchy annotated with
   * {@link SaveThisProperty @SaveThisProperty}.
   */
  public static Stream<Field> getPropertyFields(Class<?> clazz) {
    if (clazz == null) {
      return Stream.empty();
    } else {
      return Stream.concat(getPropertyFields(clazz.getSuperclass()), Arrays.stream(clazz.getDeclaredFields())
          .filter(f -> f.isAnnotationPresent(SaveThisProperty.class))
          .filter(f -> Property.class.isAssignableFrom(f.getType()))
          .peek(f -> f.setAccessible(true)));
    }
  }

  private static Stream<Field> getNestedPropertyFields(Class<?> clazz) {
    if (clazz == null) {
      return Stream.empty();
    } else {
      return Stream.concat(getNestedPropertyFields(clazz.getSuperclass()), Arrays.stream(clazz.getDeclaredFields())
          .filter(f -> f.getAnnotationsByType(SavePropertyFrom.class).length > 0));
    }
  }

  public static void serializeProperty(JsonSerializationContext context,
                                       JsonObject object,
                                       ObservableValue<?> p,
                                       Class<?> type,
                                       String name) {
    object.add(name, context.serialize(p.getValue(), TypeUtils.primitiveForBoxedType(type)));
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
  private static String getSavedName(Property<?> property, SaveThisProperty annotation) {
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
  private static String getPropertyName(SavePropertyFrom annotation) {
    if (annotation.savedName().isEmpty()) {
      return annotation.propertyName(); // Don't need to check this - it's done before this method is called
    } else {
      return annotation.savedName();
    }
  }

}
