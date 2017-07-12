package edu.wpi.first.shuffleboard.sources.recording.serialization;

import edu.wpi.first.shuffleboard.data.DataType;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class DelegatedAdapter<T> extends TypeAdapter<T> {

  private final Function<? super T, byte[]> serializer;
  private final BiFunction<byte[], Integer, ? extends T> deserializer;
  private final ToIntFunction<? super T> sizer;

  /**
   * Creates a new adapter that uses the given functions.
   *
   * @param dataType     the type of the data to serialize/deserialize
   * @param serializer   the function to use to serialize data objects to raw bytes
   * @param deserializer the function to use to deserialize raw bytes to data objects
   * @param sizer        the function to use to determine the {@link #getSerializedSize(Object) serialized sizes}
   *                     of data objects
   */
  public DelegatedAdapter(DataType<T> dataType,
                          Function<? super T, byte[]> serializer,
                          BiFunction<byte[], Integer, ? extends T> deserializer,
                          ToIntFunction<? super T> sizer) {
    super(dataType);
    this.serializer = serializer;
    this.deserializer = deserializer;
    this.sizer = sizer;
  }

  @Override
  public final T deserialize(byte[] buffer, int bufferPosition) {
    return deserializer.apply(buffer, bufferPosition);
  }

  @Override
  public final int getSerializedSize(T value) {
    return sizer.applyAsInt(value);
  }

  @Override
  public final byte[] serialize(T data) {
    return serializer.apply(data);
  }

}
