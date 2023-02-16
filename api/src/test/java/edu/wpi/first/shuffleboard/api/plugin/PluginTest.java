package edu.wpi.first.shuffleboard.api.plugin;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

public class PluginTest {

  @Test
  public void testNewPluginWithoutDescriptionThrows() {
    assertThrows(
        InvalidPluginDefinitionException.class,
        Plugin::new,
        "Creating a new plugin without a @Description annotation should throw an exception");
  }
}
