package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.HttpCamera;
import edu.wpi.first.shuffleboard.api.sources.AbstractDataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.util.EqualityUtils;
import edu.wpi.first.shuffleboard.api.util.NetworkTableUtils;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;
import edu.wpi.first.shuffleboard.plugin.cameraserver.recording.serialization.ImageConverter;
import edu.wpi.first.wpilibj.networktables.NetworkTable;
import edu.wpi.first.wpilibj.tables.ITable;
import edu.wpi.first.wpilibj.tables.ITableListener;

import org.opencv.core.Mat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import java.util.stream.Stream;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ChangeListener;
import javafx.scene.image.Image;

public final class CameraServerSource extends AbstractDataSource<CameraServerData> {

  private static final Logger log = Logger.getLogger(CameraServerSource.class.getName());

  private static final Map<String, CameraServerSource> sources = new HashMap<>();

  public static final ITable rootTable = NetworkTable.getTable("/CameraPublisher");
  private static final String[] emptyStringArray = new String[0];
  private HttpCamera camera;
  private final CvSink videoSink;
  private final Mat imageStorage = new Mat();
  private final ImageConverter imageConverter = new ImageConverter();

  private final ExecutorService frameGrabberService = Executors.newSingleThreadExecutor(ThreadUtils::makeDaemonThread);
  private final BooleanBinding enabled = active.and(connected);
  private final ITableListener streamsListener;
  private final ChangeListener<Boolean> enabledListener;
  private Future<?> frameFuture = null;
  private final ChangeListener<CameraServerData> recordingListener = (__, prev, cur) -> {
    // TODO enable after fixing recording/playback bugs
    //Recorder.getInstance().recordCurrentValue(this);
  };
  private final ITable table;

  private CameraServerSource(String name) {
    super(CameraServerDataType.INSTANCE);
    setName(name);
    table = rootTable.getSubTable(name);
    videoSink = new CvSink(name + "-videosink");

    String[] streamUrls = removeCameraProtocols(table.getStringArray("streams", emptyStringArray));
    if (streamUrls.length > 0) {
      camera = new HttpCamera(name, streamUrls);
      videoSink.setSource(camera);
    }

    streamsListener = NetworkTableUtils.createListenerEx((source, key, value, flags) -> {
      if (NetworkTableUtils.isDelete(flags) || !(value instanceof String[]) || ((String[]) value).length == 0) {
        setActive(false);
      } else {
        String[] urls = removeCameraProtocols((String[]) value);
        if (camera == null) {
          camera = new HttpCamera(name, urls);
        } else if (EqualityUtils.isDifferent(camera.getUrls(), urls)) {
          camera.setUrls(urls);
        }
        setActive(true);
      }
    });

    enabledListener = (__, was, is) -> {
      // Disable the stream when not active or not connected to save on bandwidth
      videoSink.setEnabled(is);
      if (is) {
        data.addListener(recordingListener);
        frameFuture = frameGrabberService.submit(this::grabForever);
        table.addTableListenerEx("streams", streamsListener, 0xFF);
      } else {
        data.removeListener(recordingListener);
        cancelFrameGrabber();
        table.removeTableListener(streamsListener);
      }
    };
    enabled.addListener(enabledListener);

    setActive(camera != null && camera.getUrls().length > 0);
    setConnected(true);
  }

  private void cancelFrameGrabber() {
    if (frameFuture != null) {
      frameFuture.cancel(true);
    }
  }

  /**
   * Removes leading camera protocols from an array of stream URLs. These URLs are usually in the format
   * {@code mjpg:http://...}, {@code ip:http://...}. This method will remove the leading {@code mjpg}.
   *
   * <p>This does not modify the existing array and returns a new array.
   *
   * @param streams an array of camera stream URLs to remove the
   *
   * @return the stream URLs without the leading camera protocols
   */
  private static String[] removeCameraProtocols(String... streams) {
    return Stream.of(streams)
        .map(url -> url.replaceFirst("^(mjpe?g|ip|usb):", ""))
        .toArray(String[]::new);
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
      Image image = imageConverter.convert(imageStorage);
      if (getData() == null) {
        setData(new CameraServerData(getName(), image));
      } else {
        setData(getData().withImage(image));
      }
    }
    return true;
  }

  @Override
  public void close() {
    table.removeTableListener(streamsListener);
    enabled.removeListener(enabledListener);
    cancelFrameGrabber();
    sources.remove(getName());
  }

}
