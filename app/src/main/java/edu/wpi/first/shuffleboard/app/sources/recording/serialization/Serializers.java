package edu.wpi.first.shuffleboard.app.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Keeps track of data serializers to use for saving and loading data recording files.
 */
public final class Serializers {

  private static final Map<DataType, TypeAdapter> serializers = new HashMap<>();

  private Serializers() {
  }

  /**
   * Adds the given type adapter.
   *
   * @param typeAdapter the type adapter to add
   * @param <T>         the type of the data the adapter is for
   */
  public static <T> void add(TypeAdapter<T> typeAdapter) {
    serializers.put(typeAdapter.getDataType(), typeAdapter);
  }

  /**
   * Gets the type adapter for the given data type, or {@code null} if no such adapter exists.
   *
   * @param type the data type to get the adapter for
   */
  @SuppressWarnings("unchecked")
  public static <T> TypeAdapter<T> get(DataType<T> type) {
    return serializers.get(type);
  }

  /**
   * Checks if there is an adapter for the given data type.
   */
  public static boolean hasSerializer(DataType<?> type) {
    return serializers.containsKey(type);
  }

  /**
   * Gets an optional containing the adapter for the given data type, or an empty optional if there is no such adapter.
   */
  public static <T> Optional<TypeAdapter<T>> getOptional(DataType<T> type) {
    return Optional.ofNullable(get(type));
  }

}
