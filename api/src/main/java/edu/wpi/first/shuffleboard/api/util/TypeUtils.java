package edu.wpi.first.shuffleboard.api.util;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Utilities dealing with the type system.
 */
public final class TypeUtils {

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
   * Allows you to filter out members of a subtype from a stream of some base type
   * For example, this code:
   *
   * <code>
   *   getComponents().stream()
   *     .filter(c -> c instanceof Widget)
   *     .map(c -> (Widget) c)
   *     .forEach(w -> w.setSource(...))
   * </code>
   *
   * can be turned into:
   *
   * <code>
   *   getComponents()
   *     .flatMap(TypeUtils.castStream(Widget.class))
   *     .forEach(w -> w.setSource(...))
   * </code>
   */
  public static <T, U extends T> Function<T, Stream<U>> castStream(Class<U> cls) {
    return value -> cls.isAssignableFrom(value.getClass())
            ? Stream.of(cls.cast(value))
            : Stream.empty();
  }
}
