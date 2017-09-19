package edu.wpi.first.shuffleboard.api.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MapsTest extends UtilityClassTest<Maps> {

  @Test
  public void builderEmptyTest() {
    Map<Object, Object> map = Maps.builder().build();

    assertTrue(map.isEmpty());
  }

  @Test
  public void builderTest() {
    Map<String, String> map = Maps.<String, String>builder().put("A Key", "A Value").build();

    assertEquals("A Value", map.get("A Key"));
  }

  @Test
  public void builderCompleteTest() {
    Maps.MapBuilder<String, String> mapBuilder = Maps.builder();
    mapBuilder.build();

    assertThrows(IllegalStateException.class, () -> mapBuilder.put("", ""));
  }

}
