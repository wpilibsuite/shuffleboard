package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.properties.AsyncProperty;
import edu.wpi.first.shuffleboard.api.properties.AtomicProperty;
import edu.wpi.first.shuffleboard.api.util.BitUtils;

import com.google.common.annotations.VisibleForTesting;

import edu.wpi.first.networktables.EntryListenerFlags;
import edu.wpi.first.networktables.EntryNotification;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableType;

import java.io.Closeable;
import java.util.stream.Stream;

import javafx.beans.property.ReadOnlyProperty;

/**
 * Discovers stream URLs for a specific cscore camera.
 */
public final class StreamDiscoverer implements Closeable {

  private final NetworkTableEntry streams;
  private static final String STREAMS_KEY = "streams";
  private static final String[] emptyStringArray = new String[0];

  private final AtomicProperty<String[]> urls = new AsyncProperty<>(this, "urls", emptyStringArray);
  private final int listenerHandle;

  /**
   * Creates a new stream discoverer.
   *
   * @param publisherTable the root camera server publisher table
   * @param cameraName     the name of the camera to discover streams for
   */
  public StreamDiscoverer(NetworkTable publisherTable, String cameraName) {
    streams = publisherTable.getSubTable(cameraName).getEntry(STREAMS_KEY);

    listenerHandle = streams.addListener(this::updateUrls,0xFF);
  }

  /**
   * Removes leading camera protocols from an array of stream URLs. These URLs are usually in the format
   * {@code mjpg:http://...}, {@code ip:http://...}. This method will remove the leading {@code mjpg}. This will also
   * replace a trailing <tt>/?action=stream</tt> with <tt>/stream.mjpg?</tt> due to a bug in cscore not handling URL
   * parameters correctly.
   *
   * <p>This does not modify the existing array and returns a new array.
   *
   * @param streams an array of camera stream URLs to remove the
   *
   * @return the stream URLs without the leading camera protocols
   */
  @VisibleForTesting
  static String[] removeCameraProtocols(String... streams) {
    return Stream.of(streams)
        .map(url -> url.replaceFirst("^(mjpe?g|ip|usb):", ""))
        .map(url -> url.replace("/?action=stream", "/stream.mjpg?"))
        .toArray(String[]::new);
  }

  public String[] getUrls() {
    return urls.getValue().clone(); // defensive copy
  }

  // read-only to prevent third parties from overwriting the URLs array
  public ReadOnlyProperty<String[]> urlsProperty() {
    return urls;
  }

  @Override
  public void close() {
    streams.removeListener(listenerHandle);
    urls.setValue(emptyStringArray);
  }

  /**
   * Updates the URLs from a NetworkTables entry notification.
   */
  private void updateUrls(EntryNotification notification) {
    if (BitUtils.flagMatches(notification.flags, EntryListenerFlags.kDelete)
        || notification.getEntry().getType() != NetworkTableType.kStringArray) {
      urls.setValue(emptyStringArray);
    } else {
      String[] arr = notification.getEntry().getStringArray(emptyStringArray);
      urls.setValue(removeCameraProtocols(arr));
    }
  }
}
