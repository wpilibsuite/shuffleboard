package edu.wpi.first.shuffleboard.api.data;

import edu.wpi.first.shuffleboard.api.data.types.AllType;
import edu.wpi.first.shuffleboard.api.data.types.BooleanArrayType;
import edu.wpi.first.shuffleboard.api.data.types.BooleanType;
import edu.wpi.first.shuffleboard.api.data.types.MapType;
import edu.wpi.first.shuffleboard.api.data.types.NoneType;
import edu.wpi.first.shuffleboard.api.data.types.NumberArrayType;
import edu.wpi.first.shuffleboard.api.data.types.NumberType;
import edu.wpi.first.shuffleboard.api.data.types.RawByteType;
import edu.wpi.first.shuffleboard.api.data.types.StringArrayType;
import edu.wpi.first.shuffleboard.api.data.types.StringType;
import edu.wpi.first.shuffleboard.api.data.types.UnknownType;
import edu.wpi.first.shuffleboard.api.util.Registry;
import edu.wpi.first.shuffleboard.api.util.TestUtils;
import edu.wpi.first.shuffleboard.api.util.TypeUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.common.primitives.Primitives;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Registry of data types in shuffleboard. This class also provides data types for various "wildcard" types, as well as
 * types for primitive and arrays of primitive data. These types are always registered and may not be unregistered.
 */
public class DataTypes extends Registry<DataType> {

  // TODO replace with DI eg Guice
  private static DataTypes defaultInstance = null;

  // Catchall or wildcard types
  /**
   * Represents the type of "null" or non-present data. Differs from {@link UnknownType} in that <i>no</i> data is
   * present for this type, while data <i>is</i> present but of an unknown type for the latter.
   */
  public static final DataType None = NoneType.Instance;

  /**
   * Represents the type of <i>all</i> data; a widget that can accept this data type can accept data of <i>any</i>
   * type.
   */
  public static final DataType All = AllType.Instance;

  /**
   * Represents an "unknown" data type; that is, data is present, but the type could not be determined.
   */
  public static final DataType Unknown = UnknownType.Instance;

  public static final ComplexDataType<MapData> Map = MapType.Instance;

  // Primitive types
  /**
   * The type corresponding to <i>boolean</i> data.
   */
  public static final DataType<Boolean> Boolean = BooleanType.Instance;

  /**
   * The type corresponding to a boolean array (<tt>boolean[]</tt>).
   */
  public static final DataType<boolean[]> BooleanArray = BooleanArrayType.Instance;

  /**
   * The type corresponding to <i>numeric</i> data.
   */
  public static final DataType<Number> Number = NumberType.Instance;

  /**
   * The type corresponding to an array of numeric data (<tt>double[]</tt>). Note that number arrays <i>must</i> be
   * implemented as <tt>double[]</tt> in order to be represented by this type.
   */
  public static final DataType<double[]> NumberArray = NumberArrayType.Instance;

  /**
   * The type corresponding to text data.
   */
  public static final DataType<String> String = StringType.Instance;

  /**
   * The type corresponding to an array of strings (<tt>String[]</tt>).
   */
  public static final DataType<String[]> StringArray = StringArrayType.Instance;

  /**
   * The type corresponding to an array of raw bytes (<tt>byte[]</tt>).
   */
  public static final DataType<byte[]> ByteArray = RawByteType.Instance;

  /**
   * The default data types. None of these may be unregistered.
   */
  private static final ImmutableCollection<DataType<?>> defaultTypes = ImmutableSet.of(
      // catchall
      None,
      All,
      Unknown,
      Map,
      // primitives
      Boolean,
      BooleanArray,
      Number,
      NumberArray,
      String,
      StringArray,
      ByteArray
  );

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
   * Creates a new data type registry. The registry will initially contain the default data types, none of which may be
   * unregistered.
   */
  public DataTypes() {
    registerAll(defaultTypes);
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
    requireNonNull(dataType, "dataType");
    if (defaultTypes.contains(dataType)) {
      throw new IllegalArgumentException("A default data type cannot be unregistered: '" + dataType + "'");
    }
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
        // This stream MUST be sequential to work correctly
        return dataTypes.values().stream()
            .filter(t -> t != All)
            .sorted((t1, t2) -> classComparator.reversed().compare(t1.getJavaClass(), t2.getJavaClass()))
            .findFirst();
      }
    });
  }

  /**
   * Gets a set of registered data types that can handle data of the supplied Java types.
   *
   * @see #forJavaType
   */
  public Set<DataType> forJavaTypes(Class<?>... types) {
    return Stream.of(types)
        .map(this::forJavaType)
        .flatMap(TypeUtils.optionalStream())
        .collect(Collectors.toSet());
  }

  /**
   * Creates a comparator object that compares classes in order of closest to the target class, in terms of class
   * hierarchy. For example, take a class hierarchy of
   * <pre><code>
   *   A extends Object
   *   B extends A
   *   C extends B
   *   D extends B
   * </code></pre>
   * and a collection of these four classes.
   *
   * <p><b>Scenario 1</b>
   * <br>{@code closestTo(Object.class)} would be sorted as {@code A, B, [C, D]}. The order of C, D is not
   * deterministic and depends on the ordering of the source collection, hence the brackets.
   *
   * <p><b>Scenario 2</b>
   * <br>
   * {@code closestTo(C.class)} would be sorted as {@code C, B, A, D}. {@code D} is at the very end because it is not
   * part of the class hierarchy of {@code C}; neither one subclasses the other.
   *
   * <p><b>Scenario 3</b>
   * <br>
   * {@code closestTo(B.class)} would be sorted as {@code B, [A, C], D}. {@code A} and {@code C} are equidistant from
   * {@code B}, so their ordering would depend on the order of the source collection.
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
      return Integer.compare(distance(o2, target), distance(o1, target));
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
        .flatMap(TypeUtils.optionalStream())
        .collect(Collectors.toSet());
  }

  public static boolean isCompatible(DataType type, Collection<? extends DataType> types) {
    return All.equals(type) || types.contains(All) || types.contains(type);
  }

}
