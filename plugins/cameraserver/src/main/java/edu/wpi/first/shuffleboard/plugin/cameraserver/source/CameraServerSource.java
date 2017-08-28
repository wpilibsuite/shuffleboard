package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.HttpCamera;
import edu.wpi.first.shuffleboard.api.sources.AbstractDataSource;
import edu.wpi.first.shuffleboard.api.sources.DataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.util.EqualityUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.scene.image.Image;

public class CameraServerSource extends AbstractDataSource<CameraServerData> {

  private static final Logger log = Logger.getLogger(CameraServerSource.class.getName());

  private static final Map<String, CameraServerSource> sources = new HashMap<>();

  private static final ITable rootTable = NetworkTable.getTable("/CameraServer");
  private static final String[] emptyStringArray = new String[0];
  private final HttpCamera camera;
  private final CvSink videoSink;
  private final Mat imageStorage = new Mat();

  private final ExecutorService frameGrabberService = Executors.newSingleThreadExecutor(ThreadUtils::makeDaemonThread);
  private Future<?> frameFuture = null;

  public CameraServerSource(String name) {
    super(CameraServerDataType.INSTANCE);
    setName(name);
    ITable table = rootTable.getSubTable(name);
    camera = new HttpCamera(name, table.getStringArray("urls", emptyStringArray));
    videoSink = new CvSink(name + "-videosink");
    videoSink.setSource(camera);
    table.addTableListenerEx("urls", NetworkTableUtils.createListenerEx((source, key, value, flags) -> {
      if (NetworkTableUtils.isDelete(flags) || !(value instanceof String[]) || ((String[]) value).length == 0) {
        setActive(false);
      } else {
        String[] urls = (String[]) value;
        if (EqualityUtils.isDifferent(camera.getUrls(), urls)) {
          camera.setUrls(urls);
        }
        setActive(true);
      }
    }), 0xFF);

    // Disable the stream when not active or not connected to save on bandwidth
    active.and(connected).addListener((__, was, is) -> videoSink.setEnabled(is));
    active.and(connected).addListener((__, was, is) -> {
      if (is) {
        frameFuture = frameGrabberService.submit(this::grabForever);
      } else if (frameFuture != null) {
        frameFuture.cancel(true);
      }
    });
  }

  public static CameraServerSource forName(String name) {
    return sources.computeIfAbsent(name, CameraServerSource::new);
  }

  @Override
  public SourceType getType() {
    return CameraServerSourceType.INSTANCE;
  }

  /**
   * Continuously reads frames from the MJPEG stream as long as the calling thread has not been interrupted. Because
   * this is a (nearly) infinite loop, this <strong>cannot</strong> be called from the JavaFX application thread. We
   * call this in an executor service to run asynchronously.
   */
  private void grabForever() {
    if (Platform.isFxApplicationThread()) {
      throw new IllegalStateException("This may not run on the FX application thread!");
    }
    final Thread thread = Thread.currentThread();
    while (!thread.isInterrupted()) {
      // Note:
      boolean success = grabOnceBlocking();
      if (!success) {
        // Couldn't grab the frame, wait a bit to try again
        // This may be caused by a lack of connection (such as the robot is turned off) or various other network errors
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          thread.interrupt();
        }
      }
    }
  }

  /**
   * Grabs the next frame from the MJPEG stream. This will block until a frame is received or an error occurs.
   *
   * @return true if a frame was successfully grabbed, false if an error occurred
   */
  private boolean grabOnceBlocking() {
    long frameTime = videoSink.grabFrameNoTimeout(imageStorage);
    if (frameTime == 0) {
      log.warning("Error when grabbing frame from camera '" + getName() + "': " + videoSink.getError());
      return false;
    } else {
      if (getData() == null) {
        setData(new CameraServerData(getName(), toJavaFxImage(imageStorage)));
      } else {
        setData(getData().withImage(toJavaFxImage(imageStorage)));
      }
    }
    return true;
  }

  /**
   * Creates a new JavaFX image that contains the same information as an OpenCV image.
   *
   * @param mat the OpenCV image that should be converted
   */
  private Image toJavaFxImage(Mat mat) {
    try {
      MatOfByte mob = new MatOfByte();
      Imgcodecs.imencode(".bmp", mat, mob); // bmp for lossless conversion; the stream is already compressed enough!
      return new Image(new ByteArrayInputStream(mob.toArray()));
    } finally {
      // Prod the garbage collector; there are at least two temporary byte buffers created to fit the image
      // For a 640*480 color image, this would take 921,600 bytes per buffer for a total of ~1.8MB per frame
      System.gc();
    }
  }

}
