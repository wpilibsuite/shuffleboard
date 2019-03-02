package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import edu.wpi.first.shuffleboard.plugin.cameraserver.data.Resolution;

import com.google.common.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
    commands.put("name", source.getName());
    return generateUrls(commands, baseUrls, source.getName());
  }

  @VisibleForTesting
  static String[] generateUrls(Map<String, String> commands, String[] baseUrls,
                               String cameraName) {
    if (baseUrls == null || baseUrls.length == 0) {
      return new String[0];
    }
    if (commands.isEmpty()) {
      return baseUrls;
    } else {
      var urls =  Arrays.stream(baseUrls)
          .map(url -> toHttpParams(url, commands))
          .toArray(String[]::new);
      for (var url : urls) {
        System.out.println(url);
      }
      return urls;
    }
  }

  @VisibleForTesting
  static String toHttpParams(String input, Map<String, String> commands) {
    if (commands.isEmpty()) {
      return input;
    }
    // Special case to remove name from LabVIEW camera
    if (commands.containsKey("name") && commands.get("name").contains("IMAQdx")) {
      input = input.replaceAll("\\?name=cam\\d", "");
    }
    var commandStr = commands.entrySet().stream()
        .map(e -> {
          try {
            return URLEncoder.encode(e.getKey(), "utf-8").replaceAll("\\+", "%20") + "="
                + URLEncoder.encode(e.getValue(), "utf-8").replaceAll("\\+", "%20") ;
          } catch (UnsupportedEncodingException ex) {
            return e.getKey() + "=" + e.getValue();
          }
        })
        .collect(Collectors.joining("&"));
    if (input.contains("?")) {
      return input + "&" + commandStr;
    } else {
      return input + "?" + commandStr;
    }
  }

}
