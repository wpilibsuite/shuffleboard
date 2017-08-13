package edu.wpi.first.shuffleboard.api.data;

import com.google.common.annotations.VisibleForTesting;

import edu.wpi.first.shuffleboard.api.data.types.AllType;
import edu.wpi.first.shuffleboard.api.data.types.MapType;
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
  public static final ComplexDataType<MapData> Map = new MapType();

  private static final Map<String, DataType> dataTypes = new TreeMap<>();

  private static final Map<Class, Optional<DataType>> typeCache = new HashMap<>();

  static {
    register(All);
    register(None);
    register(Unknown);
    register(Map);
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

  public static void unregister(DataType dataType) {
    dataTypes.remove(dataType.getName());
    typeCache.remove(dataType.getJavaClass());
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

  /**
   * Creates a comparator object that compares classes in order of closest to the target class, in terms of class
   * hierarchy. For example, take a class hierarchy of
   * <pre><code>
   *      Foo
   *     /   \
   *   Bar   Baz
   *    |     |
   *  Object Buq
   *          |
   *        Object
   * </code></pre>
   *
   * {@code closestTo(Object.class)} would be sorted as {@code [Bar, Buq], Baz, Foo}. The order of Bar, Baz is not
   * deterministic and depends on the ordering of the source collection, hence the brackets.
   *
   * <p><b>This method does <i>not</i> support comparison of interfaces</b></p>
   *
   * @param target the class that should be compared to to determine ordering
   */
  @VisibleForTesting
  static Comparator<Class<?>> closestTo(Class<?> target) {
    return (o1, o2) -> {
      if (o1 == o2) {
        return 0;
      }
      if (o1 == null) {
        return -1;
      } else if (o2 == null) {
        return 1;
      }
      if (o1 == target) {
        return 1;
      } else if (o2 == target) {
        return -1;
      }
      // Negate the integer comparison, otherwise the order is backwards
      return -Integer.compare(distance(o1, target), distance(o2, target));
    };
  }

  /**
   * Calculates the distance between two classes in the class hierarchy. This does <i>not</i> support interfaces. If
   * neither class subclasses the other, or either class object represents an interface, this method will return
   * {@link Integer#MAX_VALUE}.
   *
   * @param first the first class to compare
   * @param other the other class to compare
   */
  private static int distance(Class<?> first, Class<?> other) {
    if (other == first) { // NOPMD use equals() to compare references -- not null safe
      return 0;
    }
    if (first.isInterface() || other.isInterface()) {
      // Doesn't support interfaces
      return Integer.MAX_VALUE;
    }
    if (first.isAssignableFrom(other)) {
      // other superclasses first
      return distance(other.getSuperclass(), first) + 1;
    } else if (other.isAssignableFrom(first)) {
      // other subclasses first
      return distance(first.getSuperclass(), other) + 1;
    } else {
      // Neither subclasses the other
      return Integer.MAX_VALUE;
    }
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
