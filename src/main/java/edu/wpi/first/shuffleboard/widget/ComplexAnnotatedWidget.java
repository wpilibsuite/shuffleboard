package edu.wpi.first.shuffleboard.widget;

import java.util.Map;

import javafx.collections.ObservableMap;

/**
 * A complex widget that is backed by a String-to-Object map of data values.
 */
public abstract class ComplexAnnotatedWidget
    extends SimpleAnnotatedWidget<ObservableMap<String, Object>> {

  /**
   * Gets the type of the value associated with the given key, or {@code null} if there is no such
   * value.
   */
  protected Class<?> getType(String key) {
    Map<String, Object> data = getData();
    if (data.containsKey(key)) {
      return data.get(key).getClass();
    } else {
      return null;
    }
  }

  /**
   * Gets the data value associated with the given key, or {@code defaultValue} if no such value
   * exists.
   *
   * @param key          the key to get the value of
   * @param defaultValue the default value to use if no value is present
   * @param <T>          the type of the value to get
   *
   * @throws IllegalArgumentException if the implied type is not compatible with the
   */
  @SuppressWarnings("unchecked")
  protected <T> T getOrDefault(String key, T defaultValue) {
    Class<?> type = getType(key);
    if (type != defaultValue.getClass()) {
      throw new IllegalArgumentException(
          String.format(
              "Wrong type for key %s. Expected a %s but is a %s",
              key, defaultValue.getClass(), type));
    }
    return (T) getData().getOrDefault(key, defaultValue);
  }

}
