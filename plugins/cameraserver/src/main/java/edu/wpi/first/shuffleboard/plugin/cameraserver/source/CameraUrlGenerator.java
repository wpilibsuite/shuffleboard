package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.plugin.cameraserver.data.Resolution;

import com.google.common.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Generates parameterized URLs for an HTTP camera.
 */
public final class CameraUrlGenerator {

  private final CameraServerSource source;

  /**
   * Creates a new URL generator.
   *
   * @param source the source to generate URLs for
   */
  public CameraUrlGenerator(CameraServerSource source) {
    this.source = Objects.requireNonNull(source, "Null source");
  }

  /**
   * Generates parameterized URLs for the camera source.
   *
   * @param baseUrls the base stream URLs
   */
  public String[] generateUrls(String[] baseUrls) { // NOPMD varargs instead of array
    Map<String, String> commands = new LinkedHashMap<>();
    Resolution resolution = source.getTargetResolution();
    if (resolution != null && resolution.getWidth() > 0 && resolution.getHeight() > 0) {
      commands.put("resolution", resolution.getWidth() + "x" + resolution.getHeight());
    }
    int compression = source.getTargetCompression();
    if (compression >= 0 && compression <= 100) {
      commands.put("compression", Integer.toString(compression));
    }
    int frameRate = source.getTargetFps();
    if (frameRate > 0) {
      commands.put("fps", Integer.toString(frameRate));
    }
    return generateUrls(commands, baseUrls);
  }

  @VisibleForTesting
  static String[] generateUrls(Map<String, String> commands, String[] baseUrls) { // NOPMD varargs instead of array
    if (baseUrls == null || baseUrls.length == 0) {
      return new String[0];
    }
    if (commands.isEmpty()) {
      return baseUrls;
    } else {
      return Arrays.stream(baseUrls)
          .map(url -> url + toHttpParams(commands))
          .toArray(String[]::new);
    }
  }

  @VisibleForTesting
  static String toHttpParams(Map<String, String> commands) {
    if (commands.isEmpty()) {
      return "";
    }
    return commands.entrySet().stream()
        .map(e -> e.getKey() + "=" + e.getValue())
        .collect(Collectors.joining("&"));
  }

}
