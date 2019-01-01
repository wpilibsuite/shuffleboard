package edu.wpi.first.shuffleboard.app.plugin;

import edu.wpi.first.shuffleboard.api.PropertyParsers;
import edu.wpi.first.shuffleboard.api.data.DataType;
import edu.wpi.first.shuffleboard.api.data.DataTypes;
import edu.wpi.first.shuffleboard.api.plugin.Description;
import edu.wpi.first.shuffleboard.api.plugin.Plugin;
import edu.wpi.first.shuffleboard.api.plugin.Requires;
import edu.wpi.first.shuffleboard.api.sources.SourceType;
import edu.wpi.first.shuffleboard.api.sources.SourceTypes;
import edu.wpi.first.shuffleboard.api.sources.recording.Converters;
import edu.wpi.first.shuffleboard.api.tab.TabInfo;
import edu.wpi.first.shuffleboard.api.theme.Theme;
import edu.wpi.first.shuffleboard.api.theme.Themes;
import edu.wpi.first.shuffleboard.api.widget.Component;
import edu.wpi.first.shuffleboard.api.widget.ComponentType;
import edu.wpi.first.shuffleboard.api.widget.Components;
import edu.wpi.first.shuffleboard.app.tab.TabInfoRegistry;
import edu.wpi.first.shuffleboard.testplugins.BasicPlugin;
import edu.wpi.first.shuffleboard.testplugins.DependentOnUnknownPlugin;

import com.github.zafarkhaja.semver.Version;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;
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
  private TabInfoRegistry tabInfoRegistry;

  @BeforeEach
  public void setup() {
    dataTypes = new DataTypes();
    sourceTypes = new SourceTypes();
    components = new Components();
    themes = new Themes();
    tabInfoRegistry = new TabInfoRegistry();
    var converters = new Converters();
    var propertyParsers = new PropertyParsers();
    loader = new PluginLoader(dataTypes, sourceTypes, components, themes, tabInfoRegistry, converters, propertyParsers);
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
  public void testLoadJar() throws URISyntaxException, IOException {
    // given
    URL jarUrl = PluginLoaderTest.class.getResource("/test_plugins.jar");
    assumeTrue(jarUrl != null, "Test plugin jar is not present!");
    URI testPluginsJar = jarUrl.toURI();

    // when
    loader.loadPluginJar(testPluginsJar);

    // then
    assertEquals(3, loader.getKnownPlugins().size(), "All 3 plugins should have been discovered");
    assertEquals(2, loader.getLoadedPlugins().size(), "Only 2 plugins should have been loaded");
    assertAll(loader.getLoadedPlugins()
        .stream()
        .map(p ->
            () -> assertInstanceOf(
                p,
                BasicPlugin.class,
                edu.wpi.first.shuffleboard.testplugins.DependentPlugin.class
            )
        )
    );
    assertAll(loader.getKnownPlugins()
        .stream()
        .map(p ->
            () -> assertInstanceOf(
                p,
                BasicPlugin.class,
                edu.wpi.first.shuffleboard.testplugins.DependentPlugin.class,
                DependentOnUnknownPlugin.class
            )
        )
    );

  }

  @Test
  public void testLoadNewerVersionUnloadsExistingPlugin() {
    Plugin olderVersion = new MockPlugin();
    Plugin newerVersion = new NewerVersionPlugin();

    assumeTrue(loader.load(olderVersion));
    assumeTrue(loader.load(newerVersion));

    // Loading a newer version of the same plugin should unload the previously loaded one
    assertEquals(1, loader.getLoadedPlugins().size());
    assertEquals(1, loader.getKnownPlugins().size());
    assertEquals(newerVersion, loader.getKnownPlugins().get(0));
    assertTrue(loader.getLoadedPlugins().contains(newerVersion));
  }

  @Test
  public void testLoadOldVersionUnloadsExistingPlugin() {
    Plugin olderVersion = new MockPlugin();
    Plugin newerVersion = new NewerVersionPlugin();

    assumeTrue(loader.load(newerVersion));
    assumeTrue(loader.load(olderVersion));

    // Loading a newer version of the same plugin should unload the previously loaded one
    assertEquals(1, loader.getLoadedPlugins().size());
    assertEquals(1, loader.getKnownPlugins().size());
    assertEquals(olderVersion, loader.getKnownPlugins().get(0));
    assertTrue(loader.getLoadedPlugins().contains(olderVersion));
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
  public void testDefaultTabs() {
    // given
    Plugin plugin = new DefaultTabsPlugin();

    // when
    loader.load(plugin);

    // then
    assertTrue(tabInfoRegistry.isRegistered(DefaultTabsPlugin.tabInfo), "Tab info was not registered");
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
    assertEquals(0, loader.comparePluginsByDependencyGraph(MockPlugin.class, NewerVersionPlugin.class));
    assertEquals(0, loader.comparePluginsByDependencyGraph(NewerVersionPlugin.class, MockPlugin.class));
  }

  @Test
  public void testOrderPluginsByDependencies() {
    assertEquals(1, loader.comparePluginsByDependencyGraph(DependentPlugin.class, MockPlugin.class));
    assertEquals(-1, loader.comparePluginsByDependencyGraph(MockPlugin.class, DependentPlugin.class));
  }

  @Test
  public void testCyclicalDependencyThrows() {
    assertThrows(IllegalStateException.class,
        () -> loader.comparePluginsByDependencyGraph(CyclicalPluginA.class, CyclicalPluginB.class));
  }

  @Test
  public void testSelfDependencyThrows() {
    assertThrows(IllegalStateException.class,
        () -> loader.comparePluginsByDependencyGraph(SelfDependentPlugin.class, SelfDependentPlugin.class));
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
  public void testVersionCompatibilitySameMajor() {
    Version lower = Version.forIntegers(1, 0, 0);
    Version higher = Version.forIntegers(1, 1, 0);
    assertAll(() ->
        assertTrue(
            PluginLoaderHelper.isCompatible(higher, lower),
            "Version " + higher + " should be backward compatible with version " + lower
        ), () ->
        assertFalse(
            PluginLoaderHelper.isCompatible(lower, higher),
            "Version " + lower + " should not be backward compatible with version " + higher
        ));
  }

  @Test
  public void testVersionCompatibilityDifferentMajor() {
    Version lower = Version.forIntegers(1, 1, 0);
    Version higher = Version.forIntegers(lower.getMajorVersion() + 1, 1, 0);
    assertAll(() ->
        assertFalse(
            PluginLoaderHelper.isCompatible(higher, lower),
            "Version " + higher + " should not be backward compatible with version " + lower
        ), () ->
        assertFalse(
            PluginLoaderHelper.isCompatible(lower, higher),
            "Version " + lower + " should not be backward compatible with version " + higher
        ));
  }

  @Test
  public void testVersionCompatibilitySameVersion() {
    Version version = Version.forIntegers(1, 0, 0);
    Version same = Version.forIntegers(version.getMajorVersion(), version.getMinorVersion(), version.getPatchVersion());
    assertTrue(
        PluginLoaderHelper.isCompatible(version, same),
        "Identical versions should always be compatible"
    );
    assertTrue(
        PluginLoaderHelper.isCompatible(same, version),
        "Identical versions should always be compatible"
    );
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

  public static void assertInstanceOf(Object obj, Class<?>... possibleTypes) {
    for (Class<?> possibleType : possibleTypes) {
      if (possibleType.isInstance(obj)) {
        return;
      }
    }
    throw new AssertionError("Object " + obj + " is not a instance of any of: " + Arrays.toString(possibleTypes));
  }

  /**
   * A mock plugin for testing.
   */
  @Description(group = "test", name = "MockPlugin", version = "0.0.0", summary = "A plugin for testing")
  public static final class MockPlugin extends Plugin {

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

    private static final ComponentType<Component> component = new ComponentType<Component>() {

      @Override
      public Class<Component> getType() {
        return Component.class;
      }

      @Override
      public String getName() {
        return "Mock Component";
      }

      @Override
      public Component get() {
        return null;
      }
    };

    private static final Theme theme = new Theme("Mock Theme");
    private static final SourceType sourceType = new SourceType("Mock Source", false, "mock://", null) {
      @Override
      public DataType<?> dataTypeForSource(DataTypes registry, String sourceUri) {
        return DataTypes.None;
      }
    };

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

  @Description(group = "test", name = "MockPlugin", version = "9.9.9", summary = "A newer version of the mock plugin")
  public static final class NewerVersionPlugin extends Plugin {
  }

  /**
   * A plugin that depends on another plugin.
   */
  @Description(group = "test", name = "Dependent Plugin", version = "0.0.0", summary = "")
  @Requires(group = "test", name = "MockPlugin", minVersion = "0.0.0")
  public static final class DependentPlugin extends Plugin {
  }

  /**
   * A plugin that depends on another plugin, but requires a higher version than the one that gets loaded.
   */
  @Description(group = "test", name = "DependentOnHigherVersion", version = "0.0.0", summary = "")
  @Requires(group = "test", name = "MockPlugin", minVersion = "999999.999999.999999")
  public static class DependentOnHigherVersion extends Plugin {
  }

  @Description(group = "test", name = "CyclicalPluginA", version = "0.0.0", summary = "")
  @Requires(group = "test", name = "CyclicalPluginB", minVersion = "0.0.0")
  private static class CyclicalPluginA extends Plugin {
  }

  @Description(group = "test", name = "CyclicalPluginB", version = "0.0.0", summary = "")
  @Requires(group = "test", name = "CyclicalPluginA", minVersion = "0.0.0")
  private static final class CyclicalPluginB extends Plugin {
  }

  @Description(group = "test", name = "SelfDependentPlugin", version = "0.0.0", summary = "")
  @Requires(group = "test", name = "SelfDependentPlugin", minVersion = "0.0.0")
  private static final class SelfDependentPlugin extends Plugin {
  }

  @Description(group = "test", name = "DefaultTabsPlugin", version = "0.0.0", summary = "")
  private static final class DefaultTabsPlugin extends Plugin {

    private static final TabInfo tabInfo = new TabInfo("tabName", false, "sourcePrefix");

    @Override
    public List<TabInfo> getDefaultTabInfo() {
      return ImmutableList.of(
          tabInfo
      );
    }
  }


}
