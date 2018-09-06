package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Keeps track of data serializers to use for saving and loading data recording files.
 */
public final class Serializers {

  private static final Map<DataType, TypeAdapter> serializers = new HashMap<>();

  static {
    add(new SimpleAdapter<>(DataTypes.Number,
        n -> Serialization.toByteArray(n.doubleValue()), Serialization::readDouble, Serialization.SIZE_OF_DOUBLE));
    add(new SimpleAdapter<>(DataTypes.Boolean,
        Serialization::toByteArray, Serialization::readBoolean, Serialization.SIZE_OF_BOOL));
    add(new BooleanArrayAdapter());
    add(new NumberArrayAdapter());
    add(new StringAdapter());
    add(new StringArrayAdapter());
    add(new ByteArrayAdapter());
  }

  private Serializers() {
  }

  /**
   * Adds the given type adapter.
   *
   * @param typeAdapter the type adapter to add
   */
  public static void add(TypeAdapter<?> typeAdapter) {
    Objects.requireNonNull(typeAdapter, "typeAdapter");
    serializers.put(typeAdapter.getDataType(), typeAdapter);
  }

  /**
   * Removes the given type adapter.
   *
   * @param typeAdapter the type adapter to remove
   */
  public static void remove(TypeAdapter typeAdapter) {
    serializers.remove(typeAdapter.getDataType());
    typeAdapter.cleanUp();
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

  public static void cleanUpAll() {
    serializers.forEach((__, adapter) -> adapter.cleanUp());
  }

  public static Collection<TypeAdapter> getAdapters() {
    return serializers.values();
  }

}
