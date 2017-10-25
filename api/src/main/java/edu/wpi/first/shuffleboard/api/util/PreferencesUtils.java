package edu.wpi.first.shuffleboard.api.util;

import java.util.function.Function;
import java.util.prefs.Preferences;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.Property;
import javafx.beans.property.StringProperty;

/**
 * Utilities for saving and loading JavaFX properties to and from a {@link Preferences} object.
 */
public final class PreferencesUtils {

  private PreferencesUtils() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Saves a property to a preferences object.
   *
   * @param property    the property to save
   * @param preferences the preferences object to save to
   * @param serializer  a function to use to convert the property's value to a String that can be stored in
   * @param <T>         the type of the property
   *
   * @throws IllegalArgumentException if the value of the property is null
   */
  public static <T> void save(Property<? extends T> property,
                              Preferences preferences,
                              Function<? super T, String> serializer) {
    T value = property.getValue();

    if (value == null) {
      throw new IllegalArgumentException("The property must have a value");
    }

    preferences.put(property.getName(), serializer.apply(value));
  }

  /**
   * Saves an integer property.
   *
   * @param property    the property to save
   * @param preferences the preferences object to save to
   */
  public static void save(IntegerProperty property, Preferences preferences) {
    save(property, preferences, Object::toString);
  }

  /**
   * Saves a long property.
   *
   * @param property    the property to save
   * @param preferences the preferences object to save to
   */
  public static void save(LongProperty property, Preferences preferences) {
    save(property, preferences, Object::toString);
  }

  /**
   * Saves a double property.
   *
   * @param property    the property to save
   * @param preferences the preferences object to save to
   */
  public static void save(DoubleProperty property, Preferences preferences) {
    save(property, preferences, Object::toString);
  }

  /**
   * Saves a boolean property.
   *
   * @param property    the property to save
   * @param preferences the preferences object to save to
   */
  public static void save(BooleanProperty property, Preferences preferences) {
    save(property, preferences, Object::toString);
  }

  /**
   * Saves a string property.
   *
   * @param property    the property to save
   * @param preferences the preferences object to save to
   */
  public static void save(StringProperty property, Preferences preferences) {
    save(property, preferences, s -> s);
  }

  /**
   * Reads a value saved in a preferences object and stores it in a JavaFX property. If no value is present in the
   * preferences object corresponding to the property's name, or if it is of an incompatible type, the property is
   * not modified.
   *
   * @param property    the property that the read value should be placed in
   * @param preferences the preferences object to read from
   * @param parser      the function to use to convert from the serialized String to an object of the proper type
   * @param <T>         the type of the property
   */
  public static <T> void read(Property<? super T> property,
                              Preferences preferences,
                              Function<String, ? extends T> parser) {
    String name = property.getName();

    String saved = preferences.get(name, null);
    if (saved != null) {
      try {
        property.setValue(parser.apply(saved));
      } catch (Exception ignore) { // NOPMD empty catch block
        // The saved value probably wasn't the expected type
        // No need to bubble the exception up
      }
    }
  }

  /**
   * Reads an int saved in a preferences object and stores it in a JavaFX property.
   *
   * @param property    the property that the read value should be placed in
   * @param preferences the preferences object to read from
   */
  public static void read(IntegerProperty property, Preferences preferences) {
    read(property, preferences, Integer::parseInt);
  }

  /**
   * Reads a long saved in a preferences object and stores it in a JavaFX property.
   *
   * @param property    the property that the read value should be placed in
   * @param preferences the preferences object to read from
   */
  public static void read(LongProperty property, Preferences preferences) {
    read(property, preferences, Long::parseLong);
  }

  /**
   * Reads a double saved in a preferences object and stores it in a JavaFX property.
   *
   * @param property    the property that the read value should be placed in
   * @param preferences the preferences object to read from
   */
  public static void read(DoubleProperty property, Preferences preferences) {
    read(property, preferences, Double::parseDouble);
  }

  /**
   * Reads a boolean saved in a preferences object and stores it in a JavaFX property.
   *
   * @param property     the property that the read value should be placed in
   * @param preferences  the preferences object to read from
   */
  public static void read(BooleanProperty property, Preferences preferences) {
    read(property, preferences, Boolean::valueOf);
  }

  /**
   * Reads a string saved in a preferences object and stores it in a JavaFX property.
   *
   * @param property     the property that the read value should be placed in
   * @param preferences  the preferences object to read from
   */
  public static void read(StringProperty property, Preferences preferences) {
    read(property, preferences, s -> s);
  }

}
