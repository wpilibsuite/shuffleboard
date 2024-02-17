package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.api.properties.AsyncProperty;
import edu.wpi.first.shuffleboard.api.properties.AtomicProperty;

import com.google.common.annotations.VisibleForTesting;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEvent;
import edu.wpi.first.networktables.PubSubOption;
import edu.wpi.first.networktables.StringArraySubscriber;

import java.util.EnumSet;
import java.util.stream.Stream;

import javafx.beans.property.ReadOnlyProperty;

/**
 * Discovers stream URLs for a specific cscore camera.
 */
public final class StreamDiscoverer implements AutoCloseable {

  private final StringArraySubscriber streamsSub;
  private final int streamsListener;
  private static final String STREAMS_KEY = "streams";
  private static final String[] emptyStringArray = new String[0];

  private final AtomicProperty<String[]> urls = new AsyncProperty<>(this, "urls", emptyStringArray);

  /**
   * Creates a new stream discoverer.
   *
   * @param publisherTable the root camera server publisher table
   * @param cameraName     the name of the camera to discover streams for
   */
  public StreamDiscoverer(NetworkTable publisherTable, String cameraName) {
    streamsSub = publisherTable.getSubTable(cameraName).getStringArrayTopic(STREAMS_KEY).subscribe(emptyStringArray, PubSubOption.hidden(true));
    streamsListener = publisherTable.getInstance().addListener(
        streamsSub,
        EnumSet.of(
          NetworkTableEvent.Kind.kUnpublish,
          NetworkTableEvent.Kind.kValueAll,
          NetworkTableEvent.Kind.kImmediate),
        event -> {
          if (event.is(NetworkTableEvent.Kind.kUnpublish)) {
            urls.setValue(emptyStringArray);
          } else if (event.valueData != null) {
            urls.setValue(removeCameraProtocols(event.valueData.value.getStringArray()));
          }
        });
  }

  /**
   * Removes leading camera protocols from an array of stream URLs. These URLs are usually in the format
   * {@code mjpg:http://...}, {@code ip:http://...}. This method will remove the leading {@code mjpg}. This will also
   * replace a trailing <code>/?action=stream</code> with <code>/stream.mjpg?</code>
   * due to a bug in cscore not handling URL parameters correctly.
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
    streamsSub.getTopic().getInstance().removeListener(streamsListener);
    streamsSub.close();
    urls.setValue(emptyStringArray);
  }
}
