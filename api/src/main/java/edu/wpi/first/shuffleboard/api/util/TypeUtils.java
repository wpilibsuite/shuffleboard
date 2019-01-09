package edu.wpi.first.shuffleboard.api.util;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utilities dealing with the type system.
 */
public final class TypeUtils {

  private static final Map<Class<?>, Class<?>> boxedToPrimitives = Map.of(
      Byte.class, byte.class,
      Character.class, char.class,
      Short.class, short.class,
      Integer.class, int.class,
      Long.class, long.class,
      Float.class, float.class,
      Double.class, double.class,
      Boolean.class, boolean.class
  );

  private TypeUtils() {
  }

  /**
   * If 'value' can be cast into 'cls',
   * returns an Optional of that casted value. Otherwise, returns Empty.
   */
  public static <T> Optional<T> optionalCast(Object value, Class<T> cls) {
    return cls.isAssignableFrom(value.getClass())
            ? Optional.of(cls.cast(value))
            : Optional.empty();
  }

  public static <T> Function<Object, Optional<T>> optionalCast(Class<T> cls) {
    return value -> optionalCast(value, cls);
  }

  /**
   * <p>Filter out members of a subtype from a stream of some base type.
   * For example, this code:</p>
   *
   * <pre>{@code
   *   getComponents().stream()
   *     .filter(c -> c instanceof Widget)
   *     .map(c -> (Widget) c)
   *     .forEach(w -> w.setSource(...))
   * }</pre>
   *
   * <p>can be turned into:</p>
   *
   * <pre>{@code
   *   getComponents()
   *     .flatMap(TypeUtils.castStream(Widget.class))
   *     .forEach(w -> w.setSource(...))
   * }</pre>
   */
  public static <T, U extends T> Function<T, Stream<U>> castStream(Class<U> cls) {
    return value -> cls.isAssignableFrom(value.getClass())
            ? Stream.of(cls.cast(value))
            : Stream.empty();
  }

  /**
   * Turns a Stream of {@code Optional<T>}s into a Stream of the type T wrapped by the optional,
   * dropping non-present values.
   */
  public static <T> Function<Optional<T>, Stream<T>> optionalStream() {
    return value -> value.map(Stream::of).orElseGet(Stream::empty);
  }

  /**
   * Gets the primitive type for a boxed class, eg {@code primitiveForBoxedType(Integer.class) -> int.class}.
   *
   * @param boxed the boxed type
   *
   * @return the primitive type associated with the given boxed type
   */
  public static Class<?> primitiveForBoxedType(Class<?> boxed) {
    return boxedToPrimitives.getOrDefault(boxed, boxed);
  }

  /**
   * Tries to create a new instance of {@code T} using a public no-arg ("default") constructor.
   *
   * @param type the type to create a new instance of
   * @param <T>  the type of the object to be created
   *
   * @return a new instance of {@code T} created with a constructor matching the given arguments
   *
   * @throws IllegalAccessException if the constructor matching the given arguments is not public
   * @throws InstantiationException if the class is abstract
   */
  public static <T> T tryInstantiate(Class<T> type) throws IllegalAccessException, InstantiationException {
    return type.newInstance();
  }

}
