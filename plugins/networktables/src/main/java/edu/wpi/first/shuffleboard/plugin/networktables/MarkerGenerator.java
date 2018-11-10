package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.sources.recording.MarkerImportance;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Generates event markers from NetworkTables. Markers are expected in this format:
 * <br>
 * {@code /Shuffleboard/.recording/events/<name>/Info=["description", "importance"]}
 *
 * <p>The entry is expected to contain a string array with two elements: the event description as the first element, and
 * the event importance as the second. The description is allowed to be an empty string. The event importance
 * <i>must</i> be the name of one of the importance levels declared in {@link MarkerImportance}, ignoring
 * capitalization.
 *
 * <p>For example, /Shuffleboard/.recording/events/MyEvent/Info=["Something happened", "TRIVIAL"] will generate an event
 * marker with the name {@code "MyEvent"}, a description of {@code "Something happened"}, with an importance level of
 * {@link MarkerImportance#TRIVIAL}.
 */
final class MarkerGenerator {

  private static final Logger log = Logger.getLogger(MarkerGenerator.class.getName());

  public static final String EVENT_TABLE_NAME = "/Shuffleboard/.recording/events/";
  public static final String EVENT_INFO_KEY = "/Info";
  private static final String[] EMPTY = new String[0];

  // ...../events
  //        /<event name>
  //          Info = ["description", "importance"]

  private final NetworkTableInstance inst;
  private final Recorder recorder;

  private static final int LISTENER_FLAGS = EntryListenerFlags.kImmediate
      | EntryListenerFlags.kNew
      | EntryListenerFlags.kUpdate;

  private int listenerHandle = 0;

  MarkerGenerator(NetworkTableInstance inst, Recorder recorder) {
    this.inst = inst;
    this.recorder = recorder;
  }

  public void start() {
    listenerHandle = inst.addEntryListener(EVENT_TABLE_NAME, this::handleMarkerEvent, LISTENER_FLAGS);
  }

  public void stop() {
    inst.removeEntryListener(listenerHandle);
  }

  private void handleMarkerEvent(EntryNotification event) {
    if (!event.name.endsWith(EVENT_INFO_KEY)) {
      return;
    }
    String[] markerInfo = event.getEntry().getStringArray(EMPTY);
    if (markerInfo.length != 2) {
      log.warning("Malformed marker info: " + Arrays.toString(markerInfo));
      return;
    }
    List<String> hierarchy = NetworkTable.getHierarchy(event.name);
    String markerName = NetworkTable.basenameKey(hierarchy.get(hierarchy.size() - 2));
    String description = markerInfo[0];
    String importanceName = markerInfo[1];
    try {
      recorder.addMarker(markerName, description, MarkerImportance.valueOf(importanceName.toUpperCase(Locale.US)));
    } catch (IllegalArgumentException e) {
      log.warning("Invalid importance name '" + importanceName + "'");
    }
  }
}
