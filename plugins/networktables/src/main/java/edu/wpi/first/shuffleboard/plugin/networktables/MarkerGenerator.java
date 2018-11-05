package edu.wpi.first.shuffleboard.plugin.networktables;

import edu.wpi.first.shuffleboard.api.sources.recording.MarkerImportance;
import edu.wpi.first.shuffleboard.api.sources.recording.Recorder;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTableInstance;

/**
 * Generates event markers from NetworkTables. If the specified marker importance is invalid,
 * {@link MarkerImportance#NORMAL} will be used.
 */
final class MarkerGenerator {

  public static final String MARKER_ENTRY_KEY = "/Shuffleboard/.recording/EventMarker";

  private final NetworkTableInstance inst;
  private final Recorder recorder;

  private static final int LISTENER_FLAGS = EntryListenerFlags.kImmediate
      | EntryListenerFlags.kLocal
      | EntryListenerFlags.kNew
      | EntryListenerFlags.kUpdate;

  private int listenerHandle = 0;

  MarkerGenerator(NetworkTableInstance inst, Recorder recorder) {
    this.inst = inst;
    this.recorder = recorder;
  }

  public void start() {
    listenerHandle = inst.addEntryListener(MARKER_ENTRY_KEY, this::handleMarkerEvent, LISTENER_FLAGS);
  }

  public void stop() {
    inst.removeEntryListener(listenerHandle);
  }

  private void handleMarkerEvent(EntryNotification event) {
    String[] eventInfo = event.value.getStringArray();
    if (eventInfo.length < 3) {
      // Not enough info; bail
      return;
    }
    String name = eventInfo[0];
    if (name == null || name.isEmpty() || name.chars().allMatch(Character::isWhitespace)) {
      // Invalid name; bail
      return;
    }
    String description = eventInfo[1] == null ? "" : eventInfo[1];
    String importanceName = eventInfo[2];
    MarkerImportance importance = MarkerImportance.NORMAL;
    for (MarkerImportance value : MarkerImportance.values()) {
      if (value.name().equalsIgnoreCase(importanceName)) {
        importance = value;
        break;
      }
    }
    recorder.addMarker(name, description, importance);
  }
}
