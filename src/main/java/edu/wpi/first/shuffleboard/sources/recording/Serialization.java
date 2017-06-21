package edu.wpi.first.shuffleboard.sources.recording;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;

import edu.wpi.first.shuffleboard.data.DataType;
import edu.wpi.first.shuffleboard.data.DataTypes;
import edu.wpi.first.shuffleboard.data.SendableChooserData;
import edu.wpi.first.shuffleboard.util.Storage;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@SuppressWarnings("PMD.GodClass")
public final class Serialization {

  private static final Logger log = Logger.getLogger(Serialization.class.getName());

  public static final int MAGIC_NUMBER = 0xFEEDBAC4;

  private static final int SIZE_OF_BYTE = 1;
  private static final int SIZE_OF_BOOL = 1;
  private static final int SIZE_OF_INT = 4;
  private static final int SIZE_OF_LONG = 8;
  private static final int SIZE_OF_DOUBLE = 8;

  private Serialization() {
  }

  public static void saveToDefaultLocation(Recording recording) throws IOException {
    saveRecording(recording, Storage.DEFAULT_RECORDING_FILE);
  }

  /**
   * Saves a recording to the given file.
   *
   * @param recording the recording to save
   * @param file      the file to save to
   *
   * @throws IOException if the recording could not be saved to the given file
   */
  public static void saveRecording(Recording recording, String file) throws IOException {
    // work on a copy of the data so changes to the recording don't mess this up
    final List<TimestampedData> dataCopy = ImmutableList.copyOf(recording.getData());
    final byte[] header = header(dataCopy);
    List<byte[]> segments = new ArrayList<>();
    segments.add(header);
    for (TimestampedData data : dataCopy) {
      final DataType type = data.getDataType();
      final Object value = data.getData();

      final byte[] timestamp = toByteArray(data.getTimestamp());
      final byte[] sourceId = toByteArray(data.getSourceId());
      final byte[] dataType = toByteArray(type.getName());
      final byte[] dataBytes;

      if (DataTypes.Boolean.equals(type)) {
        dataBytes = toByteArray((Boolean) value);
      } else if (DataTypes.Number.equals(type)) {
        dataBytes = toByteArray(((Number) value).doubleValue());
      } else if (DataTypes.String.equals(type)) {
        dataBytes = toByteArray((String) value);
      } else if (DataTypes.RawBytes.equals(type)) {
        dataBytes = (byte[]) value;
      } else if (DataTypes.StringArray.equals(type)) {
        dataBytes = toByteArray((String[]) value);
      } else if (DataTypes.SendableChooser.equals(type)) {
        SendableChooserData sendableChooserData = (SendableChooserData) value;
        final String[] options = sendableChooserData.getOptions();
        final String defaultOption = sendableChooserData.getDefaultOption();
        final String selectedOption = sendableChooserData.getSelectedOption();
        dataBytes = Bytes.concat(toByteArray(options), toByteArray(defaultOption), toByteArray(selectedOption));
      } else {
        // Can't serialize the value, skip
        log.warning("Cannot serialize value of type " + type); //NOPMD log not surrounded by if
        continue;
      }

      segments.add(timestamp);
      segments.add(sourceId);
      segments.add(dataType);
      segments.add(dataBytes);
    }
    byte[] all = new byte[segments.stream().mapToInt(b -> b.length).sum()];
    for (int i = 0, j = 0; i < all.length; ) {
      byte[] next = segments.get(j);
      j++;
      put(all, next, i);
      i += next.length;
    }
    Files.write(Paths.get(file), all);
  }

  /**
   * Loads the default recording file at {@link Storage#DEFAULT_RECORDING_FILE}.
   *
   * @throws IOException if the file could not be read
   */
  public static Recording loadDefaultRecording() throws IOException {
    return loadRecording(Storage.DEFAULT_RECORDING_FILE);
  }

  /**
   * Loads the recording stored in the given file.
   *
   * @param file the recording file to load
   *
   * @throws IOException if the file could not be read, or if it is in an unexpected binary format
   */
  public static Recording loadRecording(String file) throws IOException {
    final byte[] bytes = Files.readAllBytes(Paths.get(file));
    final int magic = readInt(bytes, 0);
    if (magic != MAGIC_NUMBER) {
      throw new IOException("Wrong magic number in the header. Expected " + MAGIC_NUMBER + ", but was " + magic);
    }
    //final int numDataPoints = readInt(bytes, 4);
    Recording recording = new Recording();
    int cursor = 8;
    while (cursor < bytes.length) {
      final long timeStamp = readLong(bytes, cursor); // NOPMD
      cursor += SIZE_OF_LONG;
      final String sourceId = readString(bytes, cursor);
      cursor += SIZE_OF_INT + sourceId.length();
      final String dataType = readString(bytes, cursor);
      cursor += SIZE_OF_INT + dataType.length();

      final Object value;
      switch (dataType) {
        case "Boolean":
          value = readBoolean(bytes, cursor);
          cursor += SIZE_OF_BOOL;
          break;
        case "Number":
          value = readDouble(bytes, cursor);
          cursor += SIZE_OF_DOUBLE;
          break;
        case "String":
          value = readString(bytes, cursor);
          cursor += SIZE_OF_INT + ((String) value).length();
          break;
        case "StringArray":
          final String[] stringArray = readStringArray(bytes, cursor);
          cursor += sizeOfStringArray(stringArray);
          value = stringArray;
          break;
        case "SendableChooser":
          String[] options = readStringArray(bytes, cursor);
          cursor += sizeOfStringArray(options);
          String defaultOption = readString(bytes, cursor);
          cursor += SIZE_OF_INT + defaultOption.length();
          String selectedOption = readString(bytes, cursor);
          cursor += SIZE_OF_INT + selectedOption.length();
          value = new SendableChooserData(options, defaultOption, selectedOption);
          break;
        default:
          throw new IOException("Unknown data type: " + dataType);
      }

      TimestampedData data = new TimestampedData(sourceId, DataType.forName(dataType), value, timeStamp);
      recording.append(data);
    }
    return recording;
  }

  /**
   * Gets the size of a string array if it were encoded as a byte array with {@link #toByteArray(String[])}.
   */
  public static int sizeOfStringArray(String[] array) { // NOPMD varargs
    int size = SIZE_OF_INT;
    for (String s : array) {
      size += s.length() + SIZE_OF_INT;
    }
    return size;
  }

  /**
   * Generates a header for a serialized recording.
   */
  public static byte[] header(List<TimestampedData> data) {
    byte[] header = new byte[8];
    put(header, toByteArray(MAGIC_NUMBER), 0);
    put(header, toByteArray(data.size()), 4);
    return header;
  }

  /**
   * Encodes a boolean as a 1-byte array.
   */
  public static byte[] toByteArray(boolean val) {
    return new byte[]{(byte) (val ? 1 : 0)};
  }

  /**
   * Encodes a 32-bit int as a 4-byte big-endian byte array.
   *
   * @param val the int to encode
   */
  public static byte[] toByteArray(int val) {
    return new byte[]{
        (byte) ((val >> 24) & 0xFF),
        (byte) ((val >> 16) & 0xFF),
        (byte) ((val >> 8) & 0xFF),
        (byte) (val & 0xFF)
    };
  }

  /**
   * Encodes a 64-bit int as an 8-byte big-endian byte array.
   *
   * @param val the int to encode
   */
  public static byte[] toByteArray(long val) {
    return new byte[]{
        (byte) ((val >> 56) & 0xFF),
        (byte) ((val >> 48) & 0xFF),
        (byte) ((val >> 40) & 0xFF),
        (byte) ((val >> 32) & 0xFF),
        (byte) ((val >> 24) & 0xFF),
        (byte) ((val >> 16) & 0xFF),
        (byte) ((val >> 8) & 0xFF),
        (byte) (val & 0xFF)
    };
  }

  /**
   * Encodes a double-precision number as an 8-byte big-endian byte array.
   *
   * @param val the double to encode
   */
  public static byte[] toByteArray(double val) {
    return toByteArray(Double.doubleToRawLongBits(val));
  }

  /**
   * Encodes a string as a big-endian byte array. The resulting array encodes the length of the string in the first
   * four bytes, then the contents of the string.
   */
  public static byte[] toByteArray(String string) {
    try {
      return Bytes.concat(toByteArray(string.length()), string.getBytes("UTF-8"));
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("UTF-8 is not supported (the JVM is not to spec!)", e);
    }
  }

  /**
   * Encodes a string array as a big-endian byte array. These can be read with {@link #readStringArray(byte[], int)}.
   */
  public static byte[] toByteArray(String[] array) { // NOPMD varargs
    if (array.length == 0) {
      return new byte[SIZE_OF_INT];
    }
    byte[] buf = new byte[sizeOfStringArray(array)];

    int pos = 0;

    put(buf, toByteArray(array.length), pos);
    pos += SIZE_OF_INT;

    for (String string : array) {
      byte[] arr = toByteArray(string);
      put(buf, arr, pos);
      pos += arr.length;
    }

    return buf;
  }

  public static byte[] subArray(byte[] raw, int start, int end) {
    return Arrays.copyOfRange(raw, start, end);
  }

  /**
   * Puts {@code src} into {@code dst} at the given position.
   *
   * @param dst the array to be copied into
   * @param src the array to copy
   * @param pos the position in {@code dst} to copy {@code src}
   */
  public static void put(byte[] dst, byte[] src, int pos) {
    System.arraycopy(src, 0, dst, pos, src.length);
  }

  /**
   * Parses a byte array as a boolean value.
   */
  public static boolean readBoolean(byte[] array) {
    return readBoolean(array, 0);
  }

  /**
   * Reads a boolean from a byte array at the given position.
   */
  public static boolean readBoolean(byte[] array, int pos) {
    return array[pos] != 0;
  }

  /**
   * Reads a 32-bit int from a big-endian byte array.
   */
  public static int readInt(byte[] array) {
    if (array.length != SIZE_OF_INT) {
      throw new IllegalArgumentException("Required 4 bytes, but was given " + array.length);
    }
    return readInt(array, 0);
  }

  /**
   * Reads a 32-bit int from a big-endian byte array.
   */
  public static int readInt(byte[] array, int pos) {
    if (array.length < pos + SIZE_OF_INT) {
      throw new IllegalArgumentException(
          "Not enough bytes to read from. Starting position = " + pos + ", array length = " + array.length);
    }
    return (array[pos] & 0xFF) << 24
        | (array[pos + 1] & 0xFF) << 16
        | (array[pos + 2] & 0xFF) << 8
        | (array[pos + 3] & 0xFF);
  }

  /**
   * Reads a 64-bit int from a big-endian byte array.
   */
  public static long readLong(byte[] array) {
    if (array.length != SIZE_OF_LONG) {
      throw new IllegalArgumentException("Required 8 bytes, but was given " + array.length);
    }
    return readLong(array, 0);
  }

  /**
   * Reads a 64-bit int from a big-endian byte array.
   */
  public static long readLong(byte[] array, int pos) {
    if (array.length < pos + SIZE_OF_LONG) {
      throw new IllegalArgumentException(
          "Not enough bytes to read from. Starting position = " + pos + ", array length = " + array.length);
    }
    return ((long) (array[pos] & 0xFF) << 56)
        | ((long) (array[pos + 1] & 0xFF) << 48)
        | ((long) (array[pos + 2] & 0xFF) << 40)
        | ((long) (array[pos + 3] & 0xFF) << 32)
        | ((long) (array[pos + 4] & 0xFF) << 24)
        | ((long) (array[pos + 5] & 0xFF) << 16)
        | ((long) (array[pos + 6] & 0xFF) << 8)
        | (long) (array[pos + 7] & 0xFF);
  }

  /**
   * Reads a double-precision number from a big-endian byte array.
   */
  public static double readDouble(byte[] array) {
    return Double.longBitsToDouble(readLong(array));
  }

  /**
   * Reads a double-precision number from a big-endian byte array.
   */
  public static double readDouble(byte[] array, int pos) {
    return Double.longBitsToDouble(readLong(array, pos));
  }

  /**
   * Reads a string from the given byte array, starting at the given position.
   *
   * @param array the array to read from
   * @param pos   the starting position of the encoded string
   */
  public static String readString(byte[] array, int pos) {
    int cursor = pos;
    int length = readInt(array, cursor);
    cursor += SIZE_OF_INT;
    if (array.length < cursor + length) {
      throw new IllegalArgumentException(String.format(
          "Not enough bytes to read from. String length = %d, starting position = %d, array length = %d",
          length, cursor, array.length));
    }
    byte[] bytes = subArray(array, cursor, cursor + length);
    try {
      return new String(bytes, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new AssertionError("UTF-8 is not supported (the JVM is not to spec!)", e);
    }
  }

  /**
   * Reads a string array from the given byte array, starting at the given position.
   *
   * @param array the array to read from
   * @param pos   the starting position of the encoded string array
   */
  public static String[] readStringArray(byte[] array, int pos) {
    int cursor = pos;
    int length = readInt(array, cursor);
    cursor += SIZE_OF_INT;
    String[] stringArray = new String[length];
    for (int i = 0; i < length; i++) {
      String string = readString(array, cursor);
      stringArray[i] = string;
      cursor += SIZE_OF_INT + string.length();
    }
    return stringArray;
  }

}
