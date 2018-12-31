package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.DashboardMode;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Controls the {@link Recorder Shuffleboard data recorder} in response to changes in NetworkTables. Changes to the
 * file name format entry will only take effect when the next recording starts, to avoid splitting data across multiple
 * recording files.
 */
public final class RecorderController {

  public static final String DEFAULT_RECORDING_ROOT_TABLE = "/Shuffleboard/.recording";
  public static final String DEFAULT_START_STOP_KEY = DEFAULT_RECORDING_ROOT_TABLE + "/RecordData";
  public static final String DEFAULT_FILE_NAME_FORMAT_KEY = DEFAULT_RECORDING_ROOT_TABLE + "/FileNameFormat";

  private final NetworkTableEntry startStopControlEntry;
  private final NetworkTableEntry fileNameFormatEntry;
  private final Recorder recorder;
  private final MarkerGenerator markerGenerator;

  private static final int updateFlags =
      EntryListenerFlags.kImmediate
          | EntryListenerFlags.kLocal
          | EntryListenerFlags.kNew
          | EntryListenerFlags.kDelete
          | EntryListenerFlags.kUpdate;

  private int listenerHandle = 0;

  /**
   * The entry update flags from the most recent control update. This prevents us from restarting a controller every
   * time NetworkTables updates.
   */
  private int lastControlEntryFlags = -1;

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
    startStopControlEntry = ntInstance.getEntry(startStopKey);
    fileNameFormatEntry = ntInstance.getEntry(fileNameFormatKey);
    this.recorder = recorder;
    this.markerGenerator = new MarkerGenerator(ntInstance, recorder);
  }

  /**
   * Starts this controller. If the control entry is set, the recorder will immediately start if it is not already
   * running.
   */
  public void start() {
    listenerHandle = startStopControlEntry.addListener(this::updateControl, updateFlags);
    markerGenerator.start();
  }

  /**
   * Stops this controller. This does NOT stop the data recorder if it is running.
   */
  public void stop() {
    startStopControlEntry.removeListener(listenerHandle);
    markerGenerator.stop();
  }

  /**
   * Updates the state of the data recorder in response to a change to the networktable control entry. If the entry
   * does not contain a boolean value, or if Shuffleboard is in data playback mode, this method will do nothing.
   *
   * @param event the network table event
   */
  private void updateControl(EntryNotification event) {
    if (event.flags == lastControlEntryFlags) {
      return;
    }
    lastControlEntryFlags = event.flags;
    if (event.value.isBoolean() && !DashboardMode.inPlayback()) {
      if (event.value.getBoolean()) {
        recorder.stop();
        recorder.setFileNameFormat(fileNameFormatEntry.getString(Recorder.DEFAULT_RECORDING_FILE_NAME_FORMAT));
        recorder.start();
      } else {
        recorder.stop();
      }
    }
  }
}
