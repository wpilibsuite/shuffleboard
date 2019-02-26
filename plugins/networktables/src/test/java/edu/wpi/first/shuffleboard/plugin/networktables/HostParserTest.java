package edu.wpi.first.shuffleboard.plugin.networktables;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HostParserTest {

  private final HostParser parser = new HostParser();

  @Test
  public void testTeamNumber() {
    var hostInfo = parser.parse("246").get();
    assertEquals("246", hostInfo.getHost());
    assertEquals(HostParser.DEFAULT_PORT, hostInfo.getPort());
  }

  @Test
  public void testTeamAndPort() {
    var hostInfo = parser.parse("246:65535").get();
    assertEquals("246", hostInfo.getHost());
    assertEquals(65535, hostInfo.getPort());
  }

  @Test
  public void testIpAddress() {
    var hostInfo = parser.parse("10.2.46.2").get();
    assertEquals("10.2.46.2", hostInfo.getHost());
    assertEquals(HostParser.DEFAULT_PORT, hostInfo.getPort());
  }

  @Test
  public void testPortNoHost() {
    var hostInfo = parser.parse(":9999");
    assertTrue(hostInfo.isEmpty(), "No host specified should be empty");
  }

  @Test
  public void testHostNoPort() {
    var hostInfo = parser.parse("some-host:").get();
    assertEquals("some-host", hostInfo.getHost());
    assertEquals(HostParser.DEFAULT_PORT, hostInfo.getPort());
  }

  @Test
  public void testWithHttp() {
    var hostInfo = parser.parse("http://some-host:12345").get();
    assertEquals("some-host", hostInfo.getHost());
    assertEquals(12345, hostInfo.getPort());
  }

  @Test
  public void testWithHttps() {
    var hostInfo = parser.parse("https://some-host:54321").get();
    assertEquals("some-host", hostInfo.getHost());
    assertEquals(54321, hostInfo.getPort());
  }

}
