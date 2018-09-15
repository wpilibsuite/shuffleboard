package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;

import edu.wpi.cscore.CameraServerJNI;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import static edu.wpi.first.shuffleboard.plugin.cameraserver.source.CameraStreamAdapter.videoFilePath;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class CameraStreamAdapterTest {

  private CameraStreamAdapter adapter;

  @BeforeEach
  public void setup() {
    adapter = new CameraStreamAdapter();
  }

  @AfterEach
  public void tearDown() {
    adapter.cleanUp();
  }

  @Test
  public void testSerializedSize() {
    CameraServerData data = new CameraServerData("name", null, 0, 0);
    assertEquals(4 + 4 + 1 + 2 + 4 + 2, adapter.getSerializedSize(data));
  }

  @Test
  public void testSerializedDataSize() throws IOException {
    File file = File.createTempFile("test-recording", ".sbr");
    adapter.setCurrentFile(file);
    CameraServerData data = new CameraServerData("name", null, 0, 0);
    final byte[] raw = adapter.serialize(data);
    adapter.cleanUp();
    deleteTempFiles(file);
    assertEquals(17, raw.length);
  }

  @Test
  public void testDeserialize() throws IOException {
    File file = File.createTempFile("test-recording", ".sbr");
    adapter.setCurrentFile(file);
    CameraServerData data = new CameraServerData("name", null, (1 << 15) / 100, Integer.MAX_VALUE);
    byte[] bytes = adapter.serialize(data);
    adapter.cleanUp();
    CameraServerData deserialize = adapter.deserialize(bytes, 0);
    deleteTempFiles(file);
    assertAll(
        () -> assertEquals(data.getName(), deserialize.getName(), "Wrong name"),
        () -> assertEquals(data.getFps(), deserialize.getFps(), "Wrong FPS"),
        () -> assertEquals(data.getBandwidth(), deserialize.getBandwidth(), "Wrong bandwidth"),
        () -> assertNull(deserialize.getImage(), "Image should be null (video file does not exist)")
    );
  }

  @Test
  public void testEncodeRecodeReal() throws IOException {
    File file = File.createTempFile("test-recording", ".sbr");
    adapter.setCurrentFile(file);

    CameraServerJNI.getHostname(); // force load JNI
    final Mat image1 = new Mat(64, 64, CvType.CV_8UC3);
    final Mat image2 = new Mat(64, 64, CvType.CV_8UC3);
    Imgproc.rectangle(image1, new Point(0, 0), new Point(64, 64), new Scalar(0xFF, 0xFF, 0xFF), -1);
    Imgproc.rectangle(image2, new Point(0, 0), new Point(64, 64), new Scalar(0xFF, 0x00, 0x00), -1);

    final CameraServerData frame1 = new CameraServerData("name", image1, 123, 456);
    final CameraServerData frame2 = new CameraServerData("name", image2, 321, 654);

    // Copy the mats to avoid the serializer freeing them
    final Mat image1Copy = image1.clone();
    final Mat image2Copy = image2.clone();

    final byte[] frame1Bytes = adapter.serialize(frame1);
    final byte[] frame2Bytes = adapter.serialize(frame2);
    adapter.cleanUp(); // finish writing video file

    final CameraServerData deserializedFrame1 = adapter.deserialize(frame1Bytes, 0);
    final CameraServerData deserializedFrame2 = adapter.deserialize(frame2Bytes, 0);
    adapter.cleanUp(); // close video file

    Mat deserializedFrame1Image = deserializedFrame1.getImage();
    Mat deserializedFrame2Image = deserializedFrame2.getImage();

    deleteTempFiles(file);

    assertAll("Images should be present for both frames",
        () -> assertNotNull(deserializedFrame1Image, "First deserialized frame had no image"),
        () -> assertNotNull(deserializedFrame2Image, "Second deserialized frame had no image")
    );

    assertAll("Image sizes",
        () -> assertEquals(image1Copy.total(), deserializedFrame1Image.total(), "First loaded frame has wrong size"),
        () -> assertEquals(image2Copy.total(), deserializedFrame2Image.total(), "Second loaded frame has wrong size")
    );

    image1Copy.release();
    image2Copy.release();
  }

  @Test
  public void testVideoFileName() {
    assertAll("Video file names",
        () -> assertEquals(
            new File("recording-Camera.0.mp4").getAbsolutePath(),
            videoFilePath(new File("recording.sbr"), "Camera", 0)),
        () -> assertEquals(
            new File("foo/bar-a b c.45.mp4").getAbsolutePath(),
            videoFilePath(new File("foo/bar.sbr"), "a b c", 45)
        )
    );
  }

  private void deleteTempFiles(File file) {
    if (System.getenv("CI") != null) {
      // On a CI platform, don't need to clean up
      return;
    }
    boolean delete = file.delete();
    delete &= new File(videoFilePath(file, "name", 0)).delete();
    if (!delete) {
      Logger.getLogger(getClass().getName()).warning("Could not delete temporary recording files");
    }
  }

}
