package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.LazyCameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;

import com.google.common.primitives.Bytes;

import org.bytedeco.javacv.FrameRecorder;
import org.opencv.core.Mat;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.SIZE_OF_BYTE;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.SIZE_OF_INT;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.SIZE_OF_SHORT;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.readInt;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.readShort;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.readString;
import static edu.wpi.first.shuffleboard.api.sources.recording.Serialization.toByteArray;

public class CameraStreamRecorder extends TypeAdapter<CameraServerData> {

  private final Map<String, CameraStreamSaver> savers = new HashMap<>();
  private final Map<String, CameraStreamReader> readers = new HashMap<>();

  public CameraStreamRecorder() {
    super(CameraServerDataType.Instance);
  }

  @Override
  public synchronized void flush() {
    // TODO make this able to update the existing video file
    /*
    savers.values().forEach(r -> {
      try {
        r.stop();
      } catch (FrameRecorder.Exception e) {
        e.printStackTrace();
      }
    });
    savers.clear();
    started = false;
    frame = null;
    // Do not reset frame num
    */
  }

  @Override
  public synchronized void cleanUp() {
    savers.values().forEach(s -> {
      try {
        s.finish();
      } catch (FrameRecorder.Exception e) {
        e.printStackTrace();
      }
    });
    readers.clear();
  }

  @Override
  public CameraServerData deserialize(byte[] buffer, int bufferPosition) throws IOException {
    String name = readString(buffer, bufferPosition);
    bufferPosition += name.length() + SIZE_OF_INT;
    byte fileNum = buffer[bufferPosition];
    bufferPosition++;
    short frameNum = readShort(buffer, bufferPosition);
    bufferPosition += SIZE_OF_SHORT;
    int bandwidth = readInt(buffer, bufferPosition);
    bufferPosition += SIZE_OF_INT;
    double fps = readShort(buffer, bufferPosition) / 100.0;

    CameraStreamReader reader = readers.computeIfAbsent(name, __ -> new CameraStreamReader(__, getCurrentFile()));

    return new LazyCameraServerData(name, () -> {
      try {
        reader.setFileNumber(fileNum);
        return reader.readFrame(frameNum);
      } catch (IOException e) {
        e.printStackTrace();
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
  public synchronized byte[] serialize(CameraServerData data) {
    // Save:
    //  - Camera name as String
    //  - File number (0, 1, ...) as int8 (255 files will never be reached; typical count is 1)
    //  - Frame number (1, 2, 3, ...) as int16 (limits to ~9 hours)
    //  - Current bandwidth use as int32
    //  - Current FPS as int16
    // Camera URI (camera_server://CameraName) is saved by the Serializer and placed in the constant pool,
    // but we don't have access to it here
    CameraStreamSaver saver = savers.computeIfAbsent(data.getName(), name -> new CameraStreamSaver(name, getCurrentFile()));
    saver.serializeFrame(data);
    return Bytes.concat(
        toByteArray(data.getName()),
        new byte[]{(byte) saver.getFileNum()},
        toByteArray((short) saver.getFrameNum()),
        toByteArray((int) data.getBandwidth()),
        toByteArray((short) (data.getFps() * 100)) // limits to 327.68 max input FPS - should be enough :)
    );
  }

}
