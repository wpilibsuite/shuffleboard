package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.Components;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class PluginLoaderTest {

  private PluginLoader loader;
  private DataTypes dataTypes;
  private SourceTypes sourceTypes;
  private Components components;
  private Themes themes;

  @BeforeEach
  public void setup() {
    dataTypes = new DataTypes();
    sourceTypes = new SourceTypes();
    components = new Components();
    themes = new Themes();
    loader = new PluginLoader(dataTypes, sourceTypes, components, themes);
  }

  @Test
  public void testLoadClass() {
    assertTrue(loader.loadPluginClass(MockPlugin.class), "Plugin was not loaded");
    assertEquals(1, loader.getKnownPlugins().size(), "Should be 1 loaded plugin");
    assertTrue(loader.getKnownPlugins().get(0) instanceof MockPlugin, "Wrong plugin was loaded");
    assertTrue(dataTypes.isRegistered(MockPlugin.dataType));
    assertTrue(sourceTypes.isRegistered(MockPlugin.sourceType));
    assertTrue(components.isRegistered(MockPlugin.component));
    assertTrue(themes.isRegistered(MockPlugin.theme));
  }

  @Test
  public void testLoad() {
    // given
    MockPlugin plugin = new MockPlugin();

    // when
    assertTrue(loader.load(plugin), "Plugin was not loaded");

    // then
    assertTrue(plugin.isLoaded());
    assertTrue(loader.getLoadedPlugins().contains(plugin));
    assertTrue(loader.getKnownPlugins().contains(plugin));
    assertTrue(dataTypes.isRegistered(MockPlugin.dataType));
    assertTrue(sourceTypes.isRegistered(MockPlugin.sourceType));
    assertTrue(components.isRegistered(MockPlugin.component));
    assertTrue(themes.isRegistered(MockPlugin.theme));

  }

  @Test
  public void testUnload() {
    // given
    MockPlugin plugin = new MockPlugin();
    assumeTrue(loader.load(plugin));

    // when
    assertTrue(loader.unload(plugin), "The plugin was not unloaded");

    // then
    assertFalse(plugin.isLoaded());
    assertFalse(loader.getLoadedPlugins().contains(plugin));
    assertTrue(loader.getKnownPlugins().contains(plugin));
    assertFalse(dataTypes.isRegistered(MockPlugin.dataType));
    assertFalse(sourceTypes.isRegistered(MockPlugin.sourceType));
    assertFalse(components.isRegistered(MockPlugin.component));
    assertFalse(themes.isRegistered(MockPlugin.theme));
  }

  @Test
  public void testLoadDependentAlone() {
    boolean loaded = loader.load(new DependentPlugin());
    assertFalse(loaded, "The plugin should not have been loaded without having its dependents loaded");
  }

  @Test
  public void testLoadDependent() {
    assumeTrue(loader.load(new MockPlugin()), "Mock plugin wasn't loaded");
    assertTrue(loader.load(new DependentPlugin()), "Dependent plugin should have been loaded");
  }

  @Test
  public void testOrderPluginsNoDependencies() {
    Plugin p1 = new MockPlugin();
    Plugin p2 = new MockPlugin();
    assertEquals(0, PluginLoader.comparePluginsByDependencyGraph(p1, p2));
    assertEquals(0, PluginLoader.comparePluginsByDependencyGraph(p2, p1));
  }

  @Test
  public void testOrderPluginsByDependencies() {
    Plugin p1 = new MockPlugin();
    Plugin p2 = new DependentPlugin();
    assertEquals(1, PluginLoader.comparePluginsByDependencyGraph(p2, p1));
    assertEquals(-1, PluginLoader.comparePluginsByDependencyGraph(p1, p2));
  }

  @Test
  public void testCyclicalDependencyThrows() {
    // PluginA depends on PluginB
    Plugin p1 = new CyclicalPluginA();

    // PluginB depends on PluginA: cyclical dependency!
    Plugin p2 = new CyclicalPluginB();

    assertThrows(IllegalStateException.class, () -> PluginLoader.comparePluginsByDependencyGraph(p1, p2));
  }

  @Test
  public void testSelfDependencyThrows() {
    Plugin plugin = new SelfDependentPlugin();
    assertThrows(IllegalStateException.class, () -> PluginLoader.comparePluginsByDependencyGraph(plugin, plugin));
  }

  @Test
  public void testSelfDependentCannotBeLoaded() {
    Plugin plugin = new SelfDependentPlugin();
    assertFalse(loader.canLoad(plugin), "A self-dependent plugin should never be able to be loaded");
  }

  @Test
  public void testDependentOnHigherVersion() {
    assumeTrue(loader.load(new MockPlugin()));
    assertFalse(loader.load(new DependentOnHigherVersion()));
  }

  @Test
  public void testUnloadDependencyUnloadsDependents() {
    Plugin dependency = new MockPlugin();
    Plugin dependent = new DependentPlugin();
    assumeTrue(loader.load(dependency));
    assumeTrue(loader.load(dependent));
    loader.unload(dependency);
    assumeFalse(dependency.isLoaded(), "Dependency plugin is still loaded");
    assertFalse(dependent.isLoaded(), "Dependent plugin is still loaded");
  }

  /**
   * A mock plugin for testing.
   */
  public static class MockPlugin extends Plugin {

    private static final DataType<Object> dataType = new DataType<Object>("Mock Data", Object.class) {
      @Override
      public Object getDefaultValue() {
        return new Object();
      }

      @Override
      public boolean isComplex() {
        return false;
      }
    };

    private static final ComponentType component = new ComponentType() {
      @Override
      public String getName() {
        return "Mock Component";
      }

      @Override
      public Object get() {
        return null;
      }
    };

    private static final Theme theme = new Theme("Mock Theme");
    private static final SourceType sourceType = new SourceType("Mock Source", false, "mock://", null);

    public MockPlugin() {
      super("test", "Mock Plugin", "0.0.0", "A plugin for testing");
    }

    @Override
    public List<Theme> getThemes() {
      return ImmutableList.of(theme);
    }

    @Override
    public List<DataType> getDataTypes() {
      return ImmutableList.of(dataType);
    }

    @Override
    public List<ComponentType> getComponents() {
      return ImmutableList.of(component);
    }

    @Override
    public Map<DataType, ComponentType> getDefaultComponents() {
      return ImmutableMap.of(dataType, component);
    }

    @Override
    public List<SourceType> getSourceTypes() {
      return ImmutableList.of(sourceType);
    }
  }

  /**
   * A plugin that depends on another plugin.
   */
  public static class DependentPlugin extends Plugin {
    public DependentPlugin() {
      super("test", "Dependent Plugin", "0.0.0", "");
      addDependency("test:Mock Plugin:0.0.0");
    }
  }

  /**
   * A plugin that depends on another plugin, but requires a higher version than the one that gets loaded.
   */
  public static class DependentOnHigherVersion extends Plugin {
    public DependentOnHigherVersion() {
      super("test", "DependentOnHigherVersion", "0.0.0", "");
      addDependency("test:Mock Plugin:9999999.999999.99999999");
    }
  }

  private static class CyclicalPluginA extends Plugin {
    public CyclicalPluginA() {
      super("foo", "PluginA", "0.0.0", "");
      addDependency("foo:PluginB:0.0.0");
    }
  }

  private static class CyclicalPluginB extends Plugin {
    public CyclicalPluginB() {
      super("foo", "PluginB", "0.0.0", "");
      addDependency("foo:PluginA:0.0.0");
    }
  }

  private static class SelfDependentPlugin extends Plugin {
    public SelfDependentPlugin() {
      super("foo", "SelfDependent", "0.0.0", "");
      addDependency(getArtifact());
    }
  }

}
