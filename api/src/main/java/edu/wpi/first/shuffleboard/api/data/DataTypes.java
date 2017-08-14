package edu.wpi.first.shuffleboard.api.data;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.primitives.Primitives;

import edu.wpi.first.shuffleboard.api.data.types.AllType;
import edu.wpi.first.shuffleboard.api.data.types.MapType;
import edu.wpi.first.shuffleboard.api.data.types.NoneType;
import edu.wpi.first.shuffleboard.api.data.types.UnknownType;
import edu.wpi.first.shuffleboard.api.util.Registry;
import edu.wpi.first.shuffleboard.api.util.TestUtils;

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

import static java.util.Objects.requireNonNull;

public class DataTypes extends Registry<DataType> {

  // TODO replace with DI eg Guice
  private static DataTypes defaultInstance = null;

  // Catchall or wildcard types
  public static final DataType None = new NoneType();
  public static final DataType All = new AllType();
  public static final DataType Unknown = new UnknownType();
  public static final ComplexDataType<MapData> Map = new MapType();

  private final Map<String, DataType> dataTypes = new TreeMap<>();

  private final Map<Class, Optional<DataType>> typeCache = new HashMap<>();

  /**
   * Gets the default data type registry.
   */
  public static DataTypes getDefault() {
    synchronized (DataTypes.class) {
      if (defaultInstance == null) {
        defaultInstance = new DataTypes();
      }
    }
    return defaultInstance;
  }

  /**
   * Creates a new data type registry. The registry will initially contain {@link #None}, {@link #All},
   * {@link #Unknown}, and {@link #Map}, none of why may be unregistered.
   */
  public DataTypes() {
    register(None);
    register(All);
    register(Unknown);
    register(Map);
  }

  /**
   * Sets the default instance to use. <strong>This may only be called from tests</strong>.
   *
   * @throws IllegalStateException if not called from a test
   */
  @VisibleForTesting
  public static void setDefault(DataTypes instance) {
    TestUtils.assertRunningFromTest();
    defaultInstance = instance;
  }

  /**
   * Registers the given data type.
   *
   * @param dataType the data type to register
   */
  @Override
  public void register(DataType dataType) {
    requireNonNull(dataType, "dataType");
    if (isRegistered(dataType)) {
      throw new IllegalArgumentException("Data type " + dataType + " has already been registered");
    }
    dataTypes.put(dataType.getName(), dataType);
    typeCache.put(dataType.getJavaClass(), Optional.of(dataType));
    addItem(dataType);
  }

  @Override
  public void unregister(DataType dataType) {
    dataTypes.remove(dataType.getName());
    typeCache.remove(dataType.getJavaClass());
    removeItem(dataType);
  }

  /**
   * Gets the data type with the given name.
   */
  public Optional<DataType> forName(String name) {
    return Optional.ofNullable(dataTypes.get(name));
  }

  /**
   * Gets the data type most relevant to a Java class.
   */
  @SuppressWarnings("unchecked")
  public <T> Optional<DataType<T>> forJavaType(Class<T> type) {
    if (type.isPrimitive()) {
      return forJavaType((Class) Primitives.wrap(type));
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

  public void clearCache() {
    typeCache.clear();
  }

  /**
   * A.
   */
  public Set<DataType> forJavaTypes(Class<?>... types) {
    return Stream.of(types)
        .map(this::forJavaType)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }

  /**
   * Gets the registered data type of the given class.
   */
  @SuppressWarnings("unchecked")
  public <D extends DataType> Optional<D> forType(Class<D> clazz) {
    return (Optional<D>) dataTypes.values()
        .stream()
        .filter(d -> d.getClass() == clazz)
        .findFirst();
  }

  /**
   * Gets the registered data types of the given types.
   */
  public Set<DataType> forTypes(Class<? extends DataType>... types) {
    return Arrays.stream(types)
        .map(this::forType)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
  }
}
