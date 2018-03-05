package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CameraUrlGeneratorTest {

  @Test
  public void testGenerateHttpParams() {
    Map<String, String> commands = new HashMap<>();
    String params = CameraUrlGenerator.toHttpParams(commands);
    assertTrue(params.isEmpty());

    commands.put("foo", "bar");
    params = CameraUrlGenerator.toHttpParams(commands);
    assertEquals("foo=bar", params);

    commands.put("baz", "buq");
    params = CameraUrlGenerator.toHttpParams(commands);
    assertEquals("foo=bar&baz=buq", params);
  }

  @Test
  public void testGenerateUrls() {
    Map<String, String> commands = new HashMap<>();
    String url1 = "mjpeg:http://roborio-0000-frc.local:1181/stream.mjpg?";
    String url2 = "mjpeg:http://10.0.0.2:1181/stream.mjpg?";
    String[] baseUrls = {url1, url2};

    assertArrayEquals(baseUrls, CameraUrlGenerator.generateUrls(commands, baseUrls),
        "No commands should return the base URLs");

    commands.put("foo", "bar");
    assertArrayEquals(
        new String[]{url1 + "foo=bar", url2 + "foo=bar"},
        CameraUrlGenerator.generateUrls(commands, baseUrls),
        "Generated URLs do not match");
  }

}