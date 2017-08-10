package edu.wpi.first.shuffleboard.api.data;

import edu.wpi.first.shuffleboard.api.data.types.AllType;
import edu.wpi.first.shuffleboard.api.data.types.NoneType;
import edu.wpi.first.shuffleboard.api.data.types.UnknownType;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DataTypes {

  // Catchall or wildcard types
  public static final DataType None = new NoneType();
  public static final DataType All = new AllType();
  public static final DataType Unknown = new UnknownType();

  private static final Map<String, DataType> dataTypes = new TreeMap<>();

  private static final Map<Class, Optional<DataType>> typeCache = new HashMap<>();

  static {
    register(All);
    register(None);
    register(Unknown);
  }

  private DataTypes() {
  }

  /**
   * Registers the given data type.
   *
   * @param dataType the data type to register
   */
  public static void register(DataType<?> dataType) {
    dataTypes.put(dataType.getName(), dataType);
    typeCache.put(dataType.getJavaClass(), Optional.of(dataType));
  }

  /**
   * Gets the data type with the given name.
   */
  public static Optional<DataType<?>> forName(String name) {
    return Optional.ofNullable(dataTypes.get(name));
  }

  /**
   * Gets the data type most relevant to a Java class.
   */
  @SuppressWarnings("unchecked")
  public static <T> Optional<DataType<T>> forJavaType(Class<T> type) {
    if (type.isPrimitive()) {
      return forJavaType((Class) boxedType(type));
    }
    return (Optional) typeCache.computeIfAbsent(type, __ -> {
      if (DataType.class.isAssignableFrom(type)) {
        return forType((Class<DataType>) type);
      }
      Optional<DataType> naive = dataTypes.values().stream()
          .filter(t -> type.equals(t.getJavaClass()))
          .findAny();
      if (naive.isPresent()) {
        return naive;
      } else {
        // Check Java class hierarchy
        Comparator<Class<?>> classComparator = closestTo(type);
        List<DataType> sorted = dataTypes.values().stream()
            .filter(t -> t != All)
            .sorted((t1, t2) -> classComparator.reversed().compare(t1.getJavaClass(), t2.getJavaClass()))
            .collect(Collectors.toList());
        return Optional.of(sorted.get(0));
      }
    });
  }

  private static Comparator<Class<?>> closestTo(Class<?> target) {
    return (o1, o2) -> {
      if (o1 == o2) {
        return 0;
      }
      if (o1 == null) {
        return -1;
      } else if (o2 == null) {
        return 1;
      }
      boolean isO1Superclass = o1.isAssignableFrom(target);
      boolean isO2Superclass = o2.isAssignableFrom(target);
      if (isO1Superclass && !isO2Superclass) {
        return 1;
      } else if (!isO1Superclass && isO2Superclass) {
        return -1;
      } else if (!isO1Superclass) { //NOPMD
        // Neither is a superclass; order doesn't matter
        return 0;
      } else {
        // Target inherits from both
        int c1 = 0;
        int c2 = 0;
        Class sup = o1.getSuperclass();
        while (sup != Object.class && sup != null) {
          sup = sup.getSuperclass();
          c1++;
        }
        sup = o2.getSuperclass();
        while (sup != Object.class && sup != null) {
          sup = sup.getSuperclass();
          c2++;
        }
        return Integer.compare(c1, c2);
      }
    };
  }

  public static void clearCache() {
    typeCache.clear();
  }

  /**
   * A.
   */
  public static Set<DataType> forJavaTypes(Class<?>... types) {
    return Stream.of(types)
        .map(DataTypes::forJavaType)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }

  private static Class<?> boxedType(Class<?> primitiveType) {
    if (primitiveType == boolean.class) {
      return Boolean.class;
    }
    if (primitiveType == double.class) {
      return Double.class;
    }
    if (primitiveType == int.class) {
      return Integer.class;
    }
    if (primitiveType == long.class) {
      return Long.class;
    }
    return primitiveType;
  }

  /**
   * Gets the registered data type of the given class.
   */
  @SuppressWarnings("unchecked")
  public static <D extends DataType> Optional<D> forType(Class<D> clazz) {
    return (Optional<D>) dataTypes.values()
        .stream()
        .filter(d -> d.getClass() == clazz)
        .findFirst();
  }

  /**
   * Gets the registered data types of the given types.
   */
  public static Set<DataType> forTypes(Class<? extends DataType>... types) {
    return Arrays.stream(types)
        .map(DataTypes::forType)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }

}
