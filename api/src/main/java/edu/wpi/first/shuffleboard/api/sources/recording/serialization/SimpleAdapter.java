package edu.wpi.first.shuffleboard.api.sources.recording.serialization;

import edu.wpi.first.shuffleboard.api.data.DataType;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * An adapter for simple data types (eg number, boolean) that always have a specific serialized size.
 *
 * @param <T> the type of data to serialize and deserialize
 */
public final class SimpleAdapter<T> extends DelegatedAdapter<T> {

  public SimpleAdapter(DataType<T> dataType,
                       Function<T, byte[]> serializer,
                       BiFunction<byte[], Integer, T> deserializer,
                       int serializedSize) {
    super(dataType, serializer, deserializer, x -> serializedSize);
  }

}
