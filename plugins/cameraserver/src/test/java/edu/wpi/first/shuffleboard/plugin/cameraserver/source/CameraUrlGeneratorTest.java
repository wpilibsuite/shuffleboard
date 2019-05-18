package edu.wpi.first.shuffleboard.plugin.cameraserver.source;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CameraUrlGeneratorTest {

  @Test
  public void testGenerateHttpParams() {
    Map<String, String> commands = new HashMap<>();
    String baseUrl = "http://myCode:1111";
    String params = CameraUrlGenerator.addHttpParams(baseUrl, commands);
    assertEquals(baseUrl, params);

    commands.put("foo", "bar");
    params = CameraUrlGenerator.addHttpParams(baseUrl, commands);
    assertEquals(baseUrl + "?foo=bar", params);

    commands.put("baz", "buq");
    params = CameraUrlGenerator.addHttpParams(baseUrl, commands);
    assertEquals(baseUrl + "?foo=bar&baz=buq", params);
  }

  @Test
  public void testGenerateUrls() {
    Map<String, String> commands = new HashMap<>();
    String url1 = "http://roborio-0000-frc.local:1181/stream.mjpg";
    String url2 = "http://10.0.0.2:1181/stream.mjpg";
    String[] baseUrls = {url1, url2};

    assertArrayEquals(baseUrls, CameraUrlGenerator.generateUrls(commands, baseUrls),
        "No commands should return the base URLs");

    commands.put("foo", "bar");
    assertArrayEquals(
        new String[]{url1 + "?foo=bar", url2 + "?foo=bar"},
        CameraUrlGenerator.generateUrls(commands, baseUrls),
        "Generated URLs do not match");
  }

  @Test
  public void testGenerateUrlsNI() {
    String cameraName = "IMAQdx:Microsoft LifeCam HD-3000";
    Map<String, String> commands = new HashMap<>();
    commands.put("name", cameraName);
    String url1 = "http://roborio-0000-frc.local:1181/IMAQdxStream.mjpg";
    String url2 = "http://10.0.0.2:1181/IMAQdxStream.mjpg";
    String[] baseUrls = {url1, url2};


    String[] baseUrlsWithNames = Arrays.stream(baseUrls).map(x -> x + "?name=cam0").toArray(String[]::new);
    String[] baseUrlsWithCameraNames = Arrays.stream(baseUrls)
        .map(x -> x + "?name=IMAQdx%3AMicrosoft%20LifeCam%20HD-3000")
        .toArray(String[]::new);

    assertArrayEquals(baseUrlsWithCameraNames, CameraUrlGenerator.generateUrls(commands, baseUrlsWithNames),
        "Name should be replaced");
  }

}
