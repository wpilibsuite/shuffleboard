package edu.wpi.first.shuffleboard.plugin.cameraserver.recording.serialization;

import com.google.common.primitives.Bytes;

import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.sources.recording.Serialization;
import edu.wpi.first.shuffleboard.api.sources.recording.serialization.TypeAdapter;
import edu.wpi.first.shuffleboard.api.util.Maps;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;

import org.jcodec.api.JCodecException;
import org.jcodec.api.awt.AWTFrameGrab;
import org.jcodec.api.awt.AWTSequenceEncoder;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

/**
 * Serializer for camera server data. This does not encode the images, but rather a frame number that can be used to
 * extract the frame from a mp4 file.
 */
public class CameraServerDataSerializer extends TypeAdapter<CameraServerData> {

  private static final Logger log = Logger.getLogger(CameraServerDataSerializer.class.getName());

  private final Map<String, AtomicInteger> frameNumbers = new HashMap<>();
  private final Map<String, File> videoFiles = new HashMap<>();
  private final Map<String, AWTSequenceEncoder> encoders = new HashMap<>();

  public CameraServerDataSerializer() {
    super(CameraServerDataType.INSTANCE);
  }

  @Override
  public CameraServerData deserialize(byte[] buffer, int bufferPosition) {
    int pos = bufferPosition;
    String name = Serialization.readString(buffer, pos);
    pos += name.length() + Serialization.SIZE_OF_INT;
    int frameNum = Serialization.readInt(buffer, pos);
    try {
      return new CameraServerData(name, readFrame(name, frameNum));
    } catch (IOException | JCodecException e) {
      log.log(Level.WARNING, "Could not read frame " + frameNum, e);
      return new CameraServerData(name, null);
    }
  }

  @Override
  public int getSerializedSize(CameraServerData value) {
    return value.getName().length() + Serialization.SIZE_OF_INT // size of name
        + Serialization.SIZE_OF_INT; // store frame count as int
  }

  @Override
  public byte[] serialize(CameraServerData data) {
    String name = data.getName();
    Image image = data.getImage();
    int frameNum = frameNumbers.computeIfAbsent(name, __ -> new AtomicInteger(-1)).incrementAndGet();
    videoFiles.computeIfAbsent(name, n -> videoFileFor(Recorder.getInstance().getRecordingFile(), n));
    try {
      writeFrame(name, image);
    } catch (IOException e) {
      log.log(Level.SEVERE, "Could not write frame to " + videoFileNameForCamera(name), e);
    }
    return Bytes.concat(
        Serialization.toByteArray(name),
        Serialization.toByteArray(frameNum)
    );
  }

  @Override
  public void cleanUp() {
    frameNumbers.clear();
    videoFiles.clear();
    encoders.forEach((__, encoder) -> {
      try {
        encoder.finish();
      } catch (IOException e) {
        log.log(Level.SEVERE, "Could not save recording", e);
      }
    });
    encoders.clear();
  }

  private String videoFileNameForCamera(String cameraName) {
    return cameraName + ".mp4";
  }

  private File videoFileFor(File recordingFile, String cameraName) {
    return new File(recordingFile.getParent(),
        recordingFile.getName().replace(".frc", "") + "-Camera_" + videoFileNameForCamera(cameraName));
  }

  private void writeFrame(String cameraName, Image image) throws IOException {
    File file = videoFiles.get(cameraName);
    AWTSequenceEncoder encoder = Maps.computeIfAbsent(encoders, cameraName,
        __ -> AWTSequenceEncoder.createSequenceEncoder(file, 15));
    encoder.encodeImage(SwingFXUtils.fromFXImage(image, null));
  }

  private Image readFrame(String cameraName, int frameNum) throws IOException, JCodecException {
    File videoFile = videoFileFor(getCurrentFile(), cameraName);
    BufferedImage frame = AWTFrameGrab.getFrame(videoFile, frameNum);
    return SwingFXUtils.toFXImage(frame, null);
  }

}
