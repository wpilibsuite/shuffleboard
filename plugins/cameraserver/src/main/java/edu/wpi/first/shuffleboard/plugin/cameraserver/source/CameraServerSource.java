package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.DashboardMode;
import edu.wpi.first.shuffleboard.api.properties.AsyncValidatingProperty;
import edu.wpi.first.shuffleboard.api.properties.AtomicIntegerProperty;
import edu.wpi.first.shuffleboard.api.sources.AbstractDataSource;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.Sources;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;
import edu.wpi.first.shuffleboard.api.util.Debouncer;
import edu.wpi.first.shuffleboard.api.util.EqualityUtils;
import edu.wpi.first.shuffleboard.api.util.ShutdownHooks;
import edu.wpi.first.shuffleboard.api.util.ThreadUtils;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.CameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.LazyCameraServerData;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.Resolution;
import edu.wpi.first.shuffleboard.plugin.cameraserver.data.type.CameraServerDataType;

import edu.wpi.cscore.CameraServerJNI;
import edu.wpi.cscore.CvSink;
import edu.wpi.cscore.HttpCamera;
import edu.wpi.cscore.VideoEvent;
import edu.wpi.cscore.VideoException;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

import org.opencv.core.Mat;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.Property;
import javafx.beans.value.ChangeListener;

@SuppressWarnings("PMD.TooManyFields")
public final class CameraServerSource extends AbstractDataSource<CameraServerData> {

  private static final Logger log = Logger.getLogger(CameraServerSource.class.getName());

  private final NetworkTable cameraPublisherTable = NetworkTableInstance.getDefault().getTable("/CameraPublisher");
  private final int eventListenerId;
  private HttpCamera camera;
  private CvSink videoSink; // NOPMD could be final - it can't due to how lambdas handle capturing final fields
  private final Mat image = new Mat();

  private final ExecutorService frameGrabberService = Executors.newSingleThreadExecutor(ThreadUtils::makeDaemonThread);
  private final BooleanBinding enabled = active.and(connected);
  private final ChangeListener<Boolean> enabledListener = (__, was, is) -> {
    if (is) {
      reEnable();
    } else {
      cancelFrameGrabber();
    }
  };
  private Future<?> frameFuture = null;

  /**
   * The maximum supported resolution. Attempts to set the resolution higher than this will fail.
   */
  public static final Resolution MAX_RESOLUTION = new Resolution(1920, 1080);

  private final IntegerProperty targetCompression = new AtomicIntegerProperty(this, "targetCompression", -1);
  private final IntegerProperty targetFps = new AtomicIntegerProperty(this, "targetFps", -1);

  private final Property<Resolution> targetResolution =
      new AsyncValidatingProperty<>(this, "targetResolution", Resolution.EMPTY, resolution -> {
        return resolution.getWidth() <= MAX_RESOLUTION.getWidth()
            && resolution.getHeight() <= MAX_RESOLUTION.getHeight();
      });

  private final StreamDiscoverer streamDiscoverer;
  private final CameraUrlGenerator urlGenerator = new CameraUrlGenerator(this);
  private final ChangeListener<String[]> urlChangeListener = (__, old, urls) -> {
    if (urls.length == 0) {
      setActive(false);
    } else {
      String[] parameterizedUrls = urlGenerator.generateUrls(urls);
      if (camera == null) {
        camera = new HttpCamera(getName(), parameterizedUrls);
        videoSink.setSource(camera);
        videoSink.setEnabled(true);
      } else if (EqualityUtils.isDifferent(camera.getUrls(), parameterizedUrls)) {
        setCameraUrls(parameterizedUrls);
      }
      setActive(true);
    }
  };

  private volatile boolean streaming = true; // Are we currently supposed to be grabbing frames from the stream?

  // Lets us ignore kSourceDisconnected events when we force a stream reconnect to update URL parameters
  // TODO remove this when cscore fixes URL parameter parsing
  private volatile boolean forceUpdatingUrls = false;

  // Needs to be debounced; quickly changing URLs can cause serious performance hits
  private final Debouncer urlUpdateDebouncer = new Debouncer(this::updateUrls, Duration.ofMillis(10));
  private final InvalidationListener cameraUrlUpdater = __ -> urlUpdateDebouncer.run();

  CameraServerSource(String name) {
    super(CameraServerDataType.Instance);
    setName(name);
    setData(new CameraServerData(name, null, 0, 0));
    videoSink = new CvSink(name + "-videosink");
    eventListenerId = CameraServerJNI.addListener(e -> {
      if (e.name.equals(name)) {
        switch (e.kind) {
          case kSourceConnected:
            forceUpdatingUrls = false;
            setConnected(true);

            // Make sure to re-activate the stream on reconnect
            if (DashboardMode.getCurrentMode() != DashboardMode.PLAYBACK) {
              setActive(camera != null && camera.getUrls().length > 0);
            }
            break;
          case kSourceDisconnected:
            setConnected(forceUpdatingUrls);
            break;
          default:
            // don't care
            break;
        }
      }
    }, 0xFF, true);

    CameraServerJNI.addListener(e -> {
      if (enabled.get() && camera != null && camera.isValid() && streaming) {
        double bandwidth;
        double fps;
        try {
          bandwidth = camera.getActualDataRate();
        } catch (VideoException ex) {
          log.log(Level.WARNING, "Could not get bandwidth", ex);
          bandwidth = -1;
        }
        try {
          fps = camera.getActualFPS();
        } catch (VideoException ex) {
          log.log(Level.WARNING, "Could not get framerate", ex);
          fps = -1;
        }
        CameraServerData currentData = getData();
        setData(new CameraServerData(currentData.getName(), currentData.getImage(), fps, bandwidth));
      }
    }, VideoEvent.Kind.kTelemetryUpdated.getValue(), true);

    streamDiscoverer = new StreamDiscoverer(cameraPublisherTable, name);
    streamDiscoverer.urlsProperty().addListener(urlChangeListener);
    String[] streamUrls = streamDiscoverer.getUrls();
    if (streamUrls.length > 0) {
      camera = new HttpCamera(name, urlGenerator.generateUrls(streamUrls));
      videoSink.setSource(camera);
      videoSink.setEnabled(true);
    }

    DashboardMode.currentModeProperty().addListener((__, old, mode) -> {
      if (mode == DashboardMode.PLAYBACK) {
        cancelFrameGrabber();
        videoSink.setEnabled(false);
      } else {
        reEnable();
        videoSink.setEnabled(true);
      }
    });

    enabled.addListener(enabledListener);
    targetCompression.addListener(cameraUrlUpdater);
    targetFps.addListener(cameraUrlUpdater);
    targetResolution.addListener(cameraUrlUpdater);
    setActive(camera != null && camera.getUrls().length > 0);

    // If the old data is playback data, free it to reduce memory pressure
    dataProperty().addListener((__, old, data) -> {
      if (old instanceof LazyCameraServerData) {
        ((LazyCameraServerData) old).clear();
      }
    });

    ShutdownHooks.addHook(this::cancelFrameGrabber);
  }

  private void reEnable() {
    frameFuture = frameGrabberService.submit(this::grabForever);
    String[] streamUrls = streamDiscoverer.getUrls();
    if (streamUrls.length == 0) {
      setActive(false);
    } else {
      setActive(true);
    }
    streaming = true;
    streamDiscoverer.urlsProperty().addListener(urlChangeListener);
  }

  private void cancelFrameGrabber() {
    if (frameFuture != null) {
      frameFuture.cancel(true);
    }
    setActive(false);
    streaming = false;
    streamDiscoverer.urlsProperty().removeListener(urlChangeListener);
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
    while (!thread.isInterrupted() && streaming) {
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
    if (!streaming) {
      return false;
    }
    long frameTime = videoSink.grabFrameNoTimeout(image);
    if (frameTime == 0) {
      log.warning("Error when grabbing frame from camera '" + getName() + "': " + videoSink.getError());
      return false;
    } else {
      if (getData() == null) {
        setData(new CameraServerData(getName(), image, 0, 0));
      } else {
        setData(getData().withImage(image));
      }
      if (Recorder.getInstance().isRunning()) {
        Recorder.getInstance().record(getId(), getDataType(), getData().withImage(image.clone()));
      }
    }
    return true;
  }

  @Override
  public void close() {
    setActive(false);
    setConnected(false);
    streamDiscoverer.close();
    enabled.removeListener(enabledListener);
    CameraServerJNI.removeListener(eventListenerId);
    cancelFrameGrabber();
    videoSink.close();
    if (camera != null) {
      camera.close();
    }
    CameraServerSourceType.removeSource(this);
    Sources.getDefault().unregister(this);
  }

  private void updateUrls() {
    if (camera != null) {
      setCameraUrls(urlGenerator.generateUrls(streamDiscoverer.getUrls()));
    }
  }

  private void setCameraUrls(String[] urls) { // NOPMD varargs instead of array
    camera.setUrls(urls);
    forceUpdatingUrls = true;
  }

  /**
   * Gets the target compression level of the stream as set by {@link #setTargetCompression}.
   */
  public int getTargetCompression() {
    return targetCompression.get();
  }

  public IntegerProperty targetCompressionProperty() {
    return targetCompression;
  }

  /**
   * Sets the compression of the MJPEG stream, in the range [0, 100]. Lower values are lower compression. A value
   * outside this range will result in the stream using its default compression level as set in the remote program.
   *
   * @param targetCompression the compression value of the MJPEG stream
   */
  public void setTargetCompression(int targetCompression) {
    this.targetCompression.set(targetCompression);
  }

  /**
   * Gets the target FPS of the stream as set by {@link #setTargetFps}.
   */
  public int getTargetFps() {
    return targetFps.get();
  }

  public IntegerProperty targetFpsProperty() {
    return targetFps;
  }

  /**
   * Sets the output FPS of the camera. A negative or zero value will result in the camera using its default frame rate.
   *
   * @param targetFps the target FPS of the stream
   */
  public void setTargetFps(int targetFps) {
    this.targetFps.set(targetFps);
  }

  /**
   * Gets the target resolution of the stream as set by {@link #setTargetResolution}.
   *
   * @return the set target resolution
   */
  public Resolution getTargetResolution() {
    return targetResolution.getValue();
  }

  public Property<Resolution> targetResolutionProperty() {
    return targetResolution;
  }

  /**
   * Sets the target resolution of the stream. If {@code null}, or if either dimension is negative or zero, the stream
   * will use its default resolution.
   *
   * @param targetResolution the target resolution of the stream
   */
  public void setTargetResolution(Resolution targetResolution) {
    this.targetResolution.setValue(targetResolution);
  }
}
