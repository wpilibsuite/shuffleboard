package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.plugin.Plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class PluginLoaderTest {

  public static class MockPlugin extends Plugin {

    public MockPlugin() {
      super("Mock Plugin");
    }

  }

  @Test
  public void testLoadClass() {
    PluginLoader loader = new PluginLoader();
    boolean loaded = loader.loadPluginClass(MockPlugin.class);
    assertTrue(loaded, "Plugin was not loaded");
    assertEquals(1, loader.getLoadedPlugins().size(), "Should be 1 loaded plugin");
    assertTrue(loader.getLoadedPlugins().get(0) instanceof MockPlugin, "Wrong plugin was loaded");
  }

}
