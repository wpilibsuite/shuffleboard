package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.LazyCameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;

import com.google.common.primitives.Bytes;

import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.SIZE_OF_BYTE;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.SIZE_OF_INT;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.SIZE_OF_SHORT;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.readInt;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.readShort;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.readString;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.toByteArray;

/**
 * Serializer for camera streams.
 */
public final class CameraStreamAdapter extends TypeAdapter<CameraServerData> {

  private static final Logger log = Logger.getLogger(CameraStreamReader.class.getName());

  private final Map<String, CameraStreamSaver> savers = new ConcurrentHashMap<>();
  private final Map<String, CameraStreamReader> readers = new ConcurrentHashMap<>();
  private final Function<String, CameraStreamSaver> newSaver = name -> new CameraStreamSaver(name, getCurrentFile());

  public CameraStreamAdapter() {
    super(CameraServerDataType.Instance);
  }

  @Override
  public void flush() {
    // TODO make this able to update existing video files (not sure if possible with FFmpeg)
  }

  @Override
  public void cleanUp() {
    savers.forEach((name, saver) -> {
      try {
        saver.finish();
      } catch (FrameRecorder.Exception e) {
        log.log(Level.WARNING, "Could not finish saver for '" + name + "'", e);
      }
    });
    savers.clear();
    readers.forEach((name, reader) -> {
      try {
        reader.finish();
      } catch (FrameGrabber.Exception e) {
        log.log(Level.WARNING, "Could not clean up reader for '" + name + "'", e);
      }
    });
    readers.clear();
  }

  @Override
  public CameraServerData deserialize(byte[] buffer, int bufferPosition) {
    int cursor = bufferPosition;
    final String name = readString(buffer, cursor);
    cursor += name.length() + SIZE_OF_INT;
    final byte fileNum = buffer[cursor];
    cursor++;
    final short frameNum = readShort(buffer, cursor);
    cursor += SIZE_OF_SHORT;
    final int bandwidth = readInt(buffer, cursor);
    cursor += SIZE_OF_INT;
    final double fps = readShort(buffer, cursor) / 100.0;

    CameraStreamReader reader = readers.computeIfAbsent(name, __ -> new CameraStreamReader(__, getCurrentFile()));

    return new LazyCameraServerData(name, fileNum, frameNum, () -> {
      try {
        reader.setFileNumber(fileNum);
        return reader.readFrame(frameNum);
      } catch (IOException e) {
        log.log(Level.WARNING, "Could not read frame " + frameNum, e);
        return null;
      }
    }, fps, bandwidth);
  }

  @Override
  public int getSerializedSize(CameraServerData value) {
    return value.getName().length() + SIZE_OF_INT // name
        + SIZE_OF_BYTE   // video file number
        + SIZE_OF_SHORT  // frame number
        + SIZE_OF_INT    // bandwidth
        + SIZE_OF_SHORT; // FPS
  }

  @Override
  public byte[] serialize(CameraServerData data) {
    // Save:
    //  - Camera name as String
    //  - File number (0, 1, ...) as int8 (255 files will never be reached; typical count is 1)
    //  - Frame number (1, 2, 3, ...) as int16 (limits to ~9 hours)
    //  - Current bandwidth use as int32
    //  - Current FPS as int16
    // Camera URI (camera_server://CameraName) is saved by the Serializer and placed in the constant pool,
    // but we don't have access to it here
    CameraStreamSaver saver = savers.computeIfAbsent(data.getName(), newSaver);
    saver.serializeFrame(data);
    return Bytes.concat(
        toByteArray(data.getName()),
        new byte[]{(byte) saver.getFileNum()},
        toByteArray((short) saver.getLastFrameNum()),
        toByteArray((int) data.getBandwidth()),
        toByteArray((short) (data.getFps() * 100)) // limits to 327.68 max input FPS - should be enough :)
    );
  }

  /**
   * Generates the path to a video file for a recorded camera stream.
   *
   * @param rootRecordingFile the root recording file
   * @param cameraName        the name of the recorded stream
   * @param fileIndex         the video file index
   */
  public static String videoFilePath(File rootRecordingFile, String cameraName, int fileIndex) {
    return rootRecordingFile.getAbsolutePath().replace(".sbr", "-" + cameraName + "." + fileIndex + ".mp4");
  }

}
