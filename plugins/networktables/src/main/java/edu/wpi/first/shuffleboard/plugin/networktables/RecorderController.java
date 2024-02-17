package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.DashboardMode;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;

import java.util.EnumSet;

import edu.wpi.first.networktables.BooleanSubscriber;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.StringSubscriber;

/**
 * Controls the {@link Recorder Shuffleboard data recorder} in response to changes in NetworkTables. Changes to the
 * file name format entry will only take effect when the next recording starts, to avoid splitting data across multiple
 * recording files.
 */
public final class RecorderController {

  public static final String DEFAULT_RECORDING_ROOT_TABLE = "/Shuffleboard/.recording";
  public static final String DEFAULT_START_STOP_KEY = DEFAULT_RECORDING_ROOT_TABLE + "/RecordData";
  public static final String DEFAULT_FILE_NAME_FORMAT_KEY = DEFAULT_RECORDING_ROOT_TABLE + "/FileNameFormat";

  private final BooleanSubscriber startStopControlSub;
  private final StringSubscriber fileNameFormatSub;
  private final Recorder recorder;
  private final MarkerGenerator markerGenerator;

  private int listenerHandle = 0;

  /**
   * Creates a new recorder controller using the default entries {@link #DEFAULT_START_STOP_KEY} and
   * {@link #DEFAULT_FILE_NAME_FORMAT_KEY}.
   *
   * @param ntInstance the NetworkTable instance to connect to
   *
   * @return a new recorder controller listening to the default entries
   */
  public static RecorderController createWithDefaultEntries(NetworkTableInstance ntInstance) {
    return new RecorderController(
        ntInstance, DEFAULT_START_STOP_KEY, DEFAULT_FILE_NAME_FORMAT_KEY, Recorder.getInstance());
  }

  /**
   * Creates a new recorder controller.
   *
   * @param ntInstance        the NetworkTable instance to connect to
   * @param startStopKey      the key for the entry used to control the state of the recorder. The entry must contain
   *                          boolean values for any action to be taken
   * @param fileNameFormatKey the key for the entry used to control the file names of recording files. The entry must
   *                          contain a String value for it to be used
   * @param recorder          the recorder to control
   */
  public RecorderController(NetworkTableInstance ntInstance,
                            String startStopKey,
                            String fileNameFormatKey,
                            Recorder recorder) {
    startStopControlSub = ntInstance.getBooleanTopic(startStopKey).subscribe(false, PubSubOption.hidden(true));
    fileNameFormatSub = ntInstance.getStringTopic(fileNameFormatKey)
        .subscribe(Recorder.DEFAULT_RECORDING_FILE_NAME_FORMAT, PubSubOption.hidden(true));
    this.recorder = recorder;
    this.markerGenerator = new MarkerGenerator(ntInstance, recorder);
  }

  /**
   * Starts this controller. If the control entry is set, the recorder will immediately start if it is not already
   * running.
   */
  public void start() {
    listenerHandle = startStopControlSub.getTopic().getInstance().addListener(startStopControlSub,
        EnumSet.of(NetworkTableEvent.Kind.kValueAll, NetworkTableEvent.Kind.kImmediate), this::updateControl);
    markerGenerator.start();
  }

  /**
   * Stops this controller. This does NOT stop the data recorder if it is running.
   */
  public void stop() {
    startStopControlSub.getTopic().getInstance().removeListener(listenerHandle);
    markerGenerator.stop();
  }

  /**
   * Updates the state of the data recorder in response to a change to the networktable control entry. If the entry
   * does not contain a boolean value, or if Shuffleboard is in data playback mode, this method will do nothing.
   *
   * @param event the network table event
   */
  private void updateControl(NetworkTableEvent event) {
    if (!DashboardMode.inPlayback()) {
      if (event.valueData.value.getBoolean()) {
        recorder.stop();
        recorder.setFileNameFormat(fileNameFormatSub.get());
        recorder.start();
      } else {
        recorder.stop();
      }
    }
  }
}
