package edu.wpi.first.shuffleboard.util;

import java.util.Optional;

public class TypeUtils {

  public static <T> Optional<T> optionalCast(Object value, Class<T> cls) {
    return cls.isAssignableFrom(value.getClass()) ? Optional.of(cls.cast(value)) : Optional.empty();
  }
}
